package com.stellarsunset.tiff;

import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;
import com.stellarsunset.tiff.extension.TileInfo;
import com.stellarsunset.tiff.extension.tag.DifferencingPredictor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link Raster} is a rectangular array of pixels, these are the "data" inside an {@link Image}.
 *
 * <p>The {@link Image} interface is a layer to providing context around interpretation of data in the {@link Raster}.
 *
 * <p>In the baseline TIFF specification {@link Raster}s can only be encoded as separate "strips" of data but extension
 * formats (e.g. tiled images) allow images to be broken up in different ways to improve compression.
 */
public sealed interface Raster {

    /**
     * Represents a {@link Raster} as a 2D matrix of bytes. There may be multiple bytes per pixel.
     */
    record Bytes(byte[][] bytes, int componentsPerPixel) implements Raster {
    }

    /**
     * Represents a {@link Raster} as a 2D matrix of floats. There may be multiple floats per pixel.
     */
    record Floats(float[][] floats, int componentsPerPixel) implements Raster {
    }

    interface Reader {

        static ByteTilesOrStrips bytes(int componentsPerPixel) {
            return new ByteTilesOrStrips(componentsPerPixel);
        }

        static FloatTilesOrStrips floats(int componentsPerPixel) {
            return new FloatTilesOrStrips(componentsPerPixel);
        }

        /**
         * Read the {@link Raster} data associated with the image from the underlying file.
         *
         * @param channel the open channel to the bytes of the file
         * @param order   the byte order to use when interpreting data in the underlying image
         * @param ifd     the image file directory ({@link Ifd}) with tags describing the contents of the image
         */
        Raster readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd);

        record ByteTilesOrStrips(int componentsPerPixel) implements Reader {

            @Override
            public Bytes readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                if (StripInfo.getOptional(ifd).isPresent()) {
                    return new ByteStrips(componentsPerPixel).readRaster(channel, order, ifd);
                }
                if (TileInfo.getOptional(ifd).isPresent()) {
                    return new ByteTiles(componentsPerPixel).readRaster(channel, order, ifd);
                }
                throw new IllegalArgumentException(
                        "Unable to read byte contents of file, neither strip or tile layout was found."
                );
            }
        }

        record ByteStrips(int componentsPerPixel) implements Reader {

            @Override
            public Bytes readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                BytesAdapter adapter = BytesAdapter.of(order);
                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.getRequired(ifd));

                ImageDimensions imageDimensions = ImageDimensions.from(ifd);
                StripInfo stripInfo = StripInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                byte[][] bytes = new byte[intImageDimensions.length()][intImageDimensions.width() * componentsPerPixel];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int imageWidth = intImageDimensions.width();
                int widthBytes = imageWidth * componentsPerPixel;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long stripOffset = stripInfo.stripOffsets()[i];
                    int stripBytes = intStripInfo.stripByteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                    byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                    int rowsInStrip = uncompressedStrip.length / imageWidth / componentsPerPixel;
                    if (i != nOffsets - 1) {
                        checkArgument(rowsInStrip == rowsPerStrip,
                                "Incorrect number of rows found (%s) in strip# (%s).", rowsInStrip, i);
                    }

                    for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                        int imageRow = i * rowsPerStrip + stripRow;
                        int stripRowStart = stripRow * widthBytes;

                        bytes[imageRow] = Arrays.copyOfRange(
                                uncompressedStrip,
                                stripRowStart,
                                stripRowStart + widthBytes
                        );

                        predictor.unpack(bytes[imageRow]);
                    }
                }

                return new Bytes(bytes, componentsPerPixel);
            }
        }

        record ByteTiles(int componentsPerPixel) implements Reader {

            @Override
            public Bytes readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.getRequired(ifd));

                ImageDimensions imageDimensions = ImageDimensions.from(ifd);
                TileInfo tileInfo = TileInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                TileInfo.Int intTileInfo = tileInfo.asIntInfo();

                byte[][] bytes = new byte[intImageDimensions.length()][intImageDimensions.width() * componentsPerPixel];

                int nOffsets = tileInfo.offsets().length;

                int imageWidthBytes = intImageDimensions.width() * componentsPerPixel;
                int tileWidthBytes = intTileInfo.width() * componentsPerPixel;

                int oRow = 0;
                int oCol = 0;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long tileOffset = tileInfo.offsets()[i];
                    int tileBytes = intTileInfo.byteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(tileOffset, tileBytes);
                    byte[] uncompressedTile = compressor.decompress(buffer.array(), adapter);

                    checkArgument(uncompressedTile.length == tileInfo.width() * tileInfo.length() * componentsPerPixel,
                            "Incorrect number of uncompressed bytes in tile, (%s) for tile w (%s) and l (%s)",
                            uncompressedTile.length,
                            tileInfo.width(),
                            tileInfo.length()
                    );

                    for (int row = 0; row < intTileInfo.length() && oRow + row < intImageDimensions.length(); row++) {

                        int numberOfBytes = Math.min(tileWidthBytes, imageWidthBytes - oCol);
                        byte[] rowBytes = bytes[oRow + row];

                        System.arraycopy(
                                uncompressedTile,
                                row * tileWidthBytes,
                                rowBytes,
                                oCol,
                                numberOfBytes
                        );

                        predictor.unpack(rowBytes, oCol, numberOfBytes);
                    }

                    oCol += tileWidthBytes;

                    if (oCol >= imageWidthBytes) {
                        oRow += intTileInfo.length();
                        oCol = 0;
                    }
                }

                checkArgument(oCol == 0, "Should read last tile and wrap back column. oCol was %s", oCol);
                checkArgument(oRow > intImageDimensions.length(), "Should increment oRow past the end of the image, %s", oRow);

                return new Bytes(bytes, componentsPerPixel);
            }
        }

        record FloatTilesOrStrips(int componentsPerPixel) implements Reader {

            @Override
            public Floats readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                if (StripInfo.getOptional(ifd).isPresent()) {
                    return new FloatStrips(componentsPerPixel).readRaster(channel, order, ifd);
                }
                if (TileInfo.getOptional(ifd).isPresent()) {
                    return new FloatTiles(componentsPerPixel).readRaster(channel, order, ifd);
                }
                throw new IllegalArgumentException(
                        "Unable to read float contents of file, neither strip or tile layout was found."
                );
            }
        }

        record FloatStrips(int componentsPerPixel) implements Reader {

            private static final int BYTES_PER_FLOAT = 4;

            @Override
            public Floats readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.getRequired(ifd));

                ImageDimensions imageDimensions = ImageDimensions.from(ifd);
                StripInfo stripInfo = StripInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                float[][] floats = new float[intImageDimensions.length()][intImageDimensions.width() * componentsPerPixel];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int imageWidth = intImageDimensions.width();
                int widthBytes = imageWidth * BYTES_PER_FLOAT * componentsPerPixel;

                for (int i = 0; i < nOffsets; i++) {

                    long stripOffset = stripInfo.stripOffsets()[i];
                    int stripBytes = intStripInfo.stripByteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                    byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                    int rowsInStrip = uncompressedStrip.length / imageWidth / BYTES_PER_FLOAT / componentsPerPixel;
                    if (i != nOffsets - 1) {
                        checkArgument(rowsInStrip == rowsPerStrip,
                                "Incorrect number of rows found (%s) not (%s) in strip# (%s).", rowsInStrip, rowsPerStrip, i);
                    }

                    for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                        int imageRow = i * rowsPerStrip + stripRow;
                        int stripRowStart = stripRow * widthBytes;

                        floats[imageRow] = arrayAdapter.readFloats(
                                ByteBuffer.wrap(uncompressedStrip, stripRowStart, stripRowStart + widthBytes),
                                0,
                                imageWidth
                        );
                    }
                }

                return new Floats(floats, componentsPerPixel);
            }
        }

        record FloatTiles(int componentsPerPixel) implements Reader {

            private static final int BYTES_PER_FLOAT = 4;

            @Override
            public Floats readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.getRequired(ifd));

                ImageDimensions imageDimensions = ImageDimensions.from(ifd);
                TileInfo tileInfo = TileInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                TileInfo.Int intTileInfo = tileInfo.asIntInfo();

                float[][] floats = new float[intImageDimensions.length()][intImageDimensions.width() * componentsPerPixel];

                int nOffsets = tileInfo.offsets().length;

                // The current x,y coordinate of the upper-left corner of the tile in the overall
                // image array
                int oRow = 0;
                int oCol = 0;

                for (int i = 0; i < nOffsets; i++) {

                    long tileOffset = tileInfo.offsets()[i];
                    int tileBytes = intTileInfo.byteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(tileOffset, tileBytes);
                    byte[] uncompressedTile = compressor.decompress(buffer.array(), adapter);

                    checkArgument(uncompressedTile.length == tileInfo.width() * tileInfo.length() * componentsPerPixel,
                            "Incorrect number of uncompressed bytes in tile, (%s) for tile w (%s) and l (%s)",
                            uncompressedTile.length,
                            tileInfo.width(),
                            tileInfo.length()
                    );

                    for (int row = 0; row < tileInfo.width(); row++) {
//                        System.arraycopy(
//                                uncompressedTile,
//                                row * (int) tileInfo.width(),
//                                bytes[oRow + row],
//                                oCol,
//                                (int) tileInfo.width()
//                        );
                    }

                    if (oRow + intTileInfo.width() > intImageDimensions.width()) {
                        oRow = 0;
                        oCol += intTileInfo.length();
                    } else {
                        oRow += intTileInfo.width();
                    }
                }

                return new Floats(floats, componentsPerPixel);
            }
        }
    }
}
