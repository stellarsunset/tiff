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
     * Represents a {@link Raster} as a 2D matrix of shorts. There may be multiple shorts per pixel.
     */
    record Shorts(short[][] shorts, int componentsPerPixel) implements Raster {
    }

    /**
     * Represents a {@link Raster} as a 2D matrix of ints. There may be multiple ints per pixel.
     */
    record Ints(int[][] ints, int componentsPerPixel) implements Raster {
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

        static ShortTilesOrStrips shorts(int componentsPerPixel) {
            return new ShortTilesOrStrips(componentsPerPixel);
        }

        static IntTilesOrStrips ints(int componentsPerPixel) {
            return new IntTilesOrStrips(componentsPerPixel);
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
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                StripInfo stripInfo = StripInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                int imageWidth = intImageDimensions.width();
                int imageWidthBytes = imageWidth * componentsPerPixel;

                byte[][] bytes = new byte[intImageDimensions.length()][imageWidthBytes];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int widthBytes = imageWidth * componentsPerPixel;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long stripOffset = stripInfo.stripOffsets()[i];
                    int stripBytes = intStripInfo.stripByteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                    byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                    int rowsInStrip = uncompressedStrip.length / widthBytes;
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
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                TileInfo tileInfo = TileInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                TileInfo.Int intTileInfo = tileInfo.asIntInfo();

                int imageWidth = intImageDimensions.width();
                int imageWidthBytes = imageWidth * componentsPerPixel;

                byte[][] bytes = new byte[intImageDimensions.length()][imageWidthBytes];

                int nOffsets = tileInfo.offsets().length;
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

        record ShortTilesOrStrips(int componentsPerPixel) implements Reader {

            @Override
            public Shorts readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                if (StripInfo.getOptional(ifd).isPresent()) {
                    return new ShortStrips(componentsPerPixel).readRaster(channel, order, ifd);
                }
                if (TileInfo.getOptional(ifd).isPresent()) {
                    return new ShortTiles(componentsPerPixel).readRaster(channel, order, ifd);
                }
                throw new IllegalArgumentException(
                        "Unable to read short (uint16) contents of file, neither strip or tile layout was found."
                );
            }
        }

        record ShortStrips(int componentsPerPixel) implements Reader {

            private static final int BYTES_PER_SHORT = 2;

            @Override
            public Shorts readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                StripInfo stripInfo = StripInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                int imageWidth = intImageDimensions.width();
                int imageWidthShorts = imageWidth * componentsPerPixel;

                short[][] shorts = new short[intImageDimensions.length()][imageWidthShorts];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int widthBytes = imageWidthShorts * BYTES_PER_SHORT;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long stripOffset = stripInfo.stripOffsets()[i];
                    int stripBytes = intStripInfo.stripByteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                    byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                    int rowsInStrip = uncompressedStrip.length / widthBytes;
                    if (i != nOffsets - 1) {
                        checkArgument(rowsInStrip == rowsPerStrip,
                                "Incorrect number of rows found (%s) not (%s) in strip# (%s).", rowsInStrip, rowsPerStrip, i);
                    }

                    for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                        int imageRow = i * rowsPerStrip + stripRow;
                        int stripRowStart = stripRow * widthBytes;

                        shorts[imageRow] = arrayAdapter.readShorts(
                                ByteBuffer.wrap(uncompressedStrip, stripRowStart, widthBytes),
                                0,
                                imageWidthShorts
                        );
                    }
                }

                return new Shorts(shorts, componentsPerPixel);
            }
        }

        record ShortTiles(int componentsPerPixel) implements Reader {

            private static final int BYTES_PER_SHORT = 2;

            @Override
            public Shorts readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                TileInfo tileInfo = TileInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                TileInfo.Int intTileInfo = tileInfo.asIntInfo();

                int imageWidthShorts = intImageDimensions.width() * componentsPerPixel;
                int imageWidthBytes = imageWidthShorts * BYTES_PER_SHORT;

                short[][] shorts = new short[intImageDimensions.length()][imageWidthShorts];

                int nOffsets = tileInfo.offsets().length;

                // The current x,y coordinate of the upper-left corner of the tile in the overall
                // image array
                int oRow = 0;
                int oCol = 0;

                int tileWidthShorts = intTileInfo.width() * componentsPerPixel;
                int tileWidthBytes = tileWidthShorts * BYTES_PER_SHORT;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long tileOffset = tileInfo.offsets()[i];
                    int tileBytes = intTileInfo.byteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(tileOffset, tileBytes);
                    byte[] uncompressedTile = compressor.decompress(buffer.array(), adapter);

                    checkArgument(uncompressedTile.length == tileWidthBytes * intTileInfo.length(),
                            "Incorrect number of uncompressed bytes in tile, (%s) for tile w (%s) and l (%s)",
                            uncompressedTile.length,
                            tileInfo.width(),
                            tileInfo.length()
                    );

                    for (int row = 0; row < intTileInfo.length() && oRow + row < intImageDimensions.length(); row++) {

                        int tileRowStart = row * tileWidthBytes;

                        short[] fRow = arrayAdapter.readShorts(
                                ByteBuffer.wrap(uncompressedTile, tileRowStart, tileWidthBytes),
                                0,
                                tileWidthShorts
                        );

                        int numberOfInts = Math.min(tileWidthShorts, imageWidthShorts - oCol);

                        System.arraycopy(
                                fRow,
                                0,
                                shorts[oRow + row],
                                oCol,
                                numberOfInts
                        );
                    }

                    oCol += tileWidthShorts;

                    if (oCol >= imageWidthShorts) {
                        oRow += intTileInfo.length();
                        oCol = 0;
                    }
                }

                return new Shorts(shorts, componentsPerPixel);
            }
        }

        record IntTilesOrStrips(int componentsPerPixel) implements Reader {

            @Override
            public Ints readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                if (StripInfo.getOptional(ifd).isPresent()) {
                    return new IntStrips(componentsPerPixel).readRaster(channel, order, ifd);
                }
                if (TileInfo.getOptional(ifd).isPresent()) {
                    return new IntTiles(componentsPerPixel).readRaster(channel, order, ifd);
                }
                throw new IllegalArgumentException(
                        "Unable to read integer (uint32) contents of file, neither strip or tile layout was found."
                );
            }
        }

        record IntStrips(int componentsPerPixel) implements Reader {

            private static final int BYTES_PER_INT = 4;

            @Override
            public Ints readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                StripInfo stripInfo = StripInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                int imageWidth = intImageDimensions.width();
                int imageWidthInts = imageWidth * componentsPerPixel;

                int[][] ints = new int[intImageDimensions.length()][imageWidthInts];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int widthBytes = imageWidthInts * BYTES_PER_INT;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long stripOffset = stripInfo.stripOffsets()[i];
                    int stripBytes = intStripInfo.stripByteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                    byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                    int rowsInStrip = uncompressedStrip.length / imageWidth / BYTES_PER_INT / componentsPerPixel;
                    if (i != nOffsets - 1) {
                        checkArgument(rowsInStrip == rowsPerStrip,
                                "Incorrect number of rows found (%s) not (%s) in strip# (%s).", rowsInStrip, rowsPerStrip, i);
                    }

                    for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                        int imageRow = i * rowsPerStrip + stripRow;
                        int stripRowStart = stripRow * widthBytes;

                        ints[imageRow] = arrayAdapter.readInts(
                                ByteBuffer.wrap(uncompressedStrip, stripRowStart, widthBytes),
                                0,
                                imageWidthInts
                        );
                    }
                }

                return new Ints(ints, componentsPerPixel);
            }
        }

        record IntTiles(int componentsPerPixel) implements Reader {

            private static final int BYTES_PER_INT = 4;

            @Override
            public Ints readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

                BytesAdapter adapter = BytesAdapter.of(order);
                ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                TileInfo tileInfo = TileInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                TileInfo.Int intTileInfo = tileInfo.asIntInfo();

                int imageWidthInts = intImageDimensions.width() * componentsPerPixel;
                int imageWidthBytes = imageWidthInts * BYTES_PER_INT;

                int[][] ints = new int[intImageDimensions.length()][imageWidthInts];

                int nOffsets = tileInfo.offsets().length;

                // The current x,y coordinate of the upper-left corner of the tile in the overall
                // image array
                int oRow = 0;
                int oCol = 0;

                int tileWidthInts = intTileInfo.width() * componentsPerPixel;
                int tileWidthBytes = tileWidthInts * BYTES_PER_INT;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long tileOffset = tileInfo.offsets()[i];
                    int tileBytes = intTileInfo.byteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(tileOffset, tileBytes);
                    byte[] uncompressedTile = compressor.decompress(buffer.array(), adapter);

                    checkArgument(uncompressedTile.length == tileWidthBytes * intTileInfo.length(),
                            "Incorrect number of uncompressed bytes in tile, (%s) for tile w (%s) and l (%s)",
                            uncompressedTile.length,
                            tileInfo.width(),
                            tileInfo.length()
                    );

                    for (int row = 0; row < intTileInfo.length() && oRow + row < intImageDimensions.length(); row++) {

                        int tileRowStart = row * tileWidthBytes;

                        int[] fRow = arrayAdapter.readInts(
                                ByteBuffer.wrap(uncompressedTile, tileRowStart, tileWidthBytes),
                                0,
                                tileWidthInts
                        );

                        int numberOfInts = Math.min(tileWidthInts, imageWidthInts - oCol);

                        System.arraycopy(
                                fRow,
                                0,
                                ints[oRow + row],
                                oCol,
                                numberOfInts
                        );
                    }

                    oCol += tileWidthInts;

                    if (oCol >= imageWidthInts) {
                        oRow += intTileInfo.length();
                        oCol = 0;
                    }
                }

                return new Ints(ints, componentsPerPixel);
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
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                StripInfo stripInfo = StripInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                int imageWidth = intImageDimensions.width();
                int imageWidthFloats = imageWidth * componentsPerPixel;

                float[][] floats = new float[intImageDimensions.length()][imageWidthFloats];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int widthBytes = imageWidthFloats * BYTES_PER_FLOAT;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

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
                                ByteBuffer.wrap(uncompressedStrip, stripRowStart, widthBytes),
                                0,
                                imageWidthFloats
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
                ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.get(ifd));

                ImageDimensions imageDimensions = ImageDimensions.get(ifd);
                TileInfo tileInfo = TileInfo.getRequired(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                TileInfo.Int intTileInfo = tileInfo.asIntInfo();

                int imageWidthFloats = intImageDimensions.width() * componentsPerPixel;
                int imageWidthBytes = imageWidthFloats * BYTES_PER_FLOAT;

                float[][] floats = new float[intImageDimensions.length()][imageWidthFloats];

                int nOffsets = tileInfo.offsets().length;

                // The current x,y coordinate of the upper-left corner of the tile in the overall
                // image array
                int oRow = 0;
                int oCol = 0;

                int tileWidthFloats = intTileInfo.width() * componentsPerPixel;
                int tileWidthBytes = tileWidthFloats * BYTES_PER_FLOAT;

                DifferencingPredictor predictor = DifferencingPredictor.get(ifd);

                for (int i = 0; i < nOffsets; i++) {

                    long tileOffset = tileInfo.offsets()[i];
                    int tileBytes = intTileInfo.byteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(tileOffset, tileBytes);
                    byte[] uncompressedTile = compressor.decompress(buffer.array(), adapter);

                    checkArgument(uncompressedTile.length == tileWidthBytes * intTileInfo.length(),
                            "Incorrect number of uncompressed bytes in tile, (%s) for tile w (%s) and l (%s)",
                            uncompressedTile.length,
                            tileInfo.width(),
                            tileInfo.length()
                    );

                    for (int row = 0; row < intTileInfo.length() && oRow + row < intImageDimensions.length(); row++) {

                        int tileRowStart = row * tileWidthBytes;

                        float[] fRow = arrayAdapter.readFloats(
                                ByteBuffer.wrap(uncompressedTile, tileRowStart, tileWidthBytes),
                                0,
                                tileWidthFloats
                        );

                        int numberOfFloats = Math.min(tileWidthFloats, imageWidthFloats - oCol);

                        System.arraycopy(
                                fRow,
                                0,
                                floats[oRow + row],
                                oCol,
                                numberOfFloats
                        );
                    }

                    oCol += tileWidthFloats;

                    if (oCol >= imageWidthFloats) {
                        oRow += intTileInfo.length();
                        oCol = 0;
                    }
                }

                return new Floats(floats, componentsPerPixel);
            }
        }
    }
}
