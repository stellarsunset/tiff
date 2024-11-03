package com.stellarsunset.tiff;

import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;

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

        /**
         * Read the {@link Raster} data associated with the image from the underlying file.
         *
         * @param channel the open channel to the bytes of the file
         * @param order   the byte order to use when interpreting data in the underlying image
         * @param ifd     the image file directory ({@link Ifd}) with tags describing the contents of the image
         */
        Raster readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd);

        record ByteStrips(int componentsPerPixel) implements Reader {

            @Override
            public Bytes readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                BytesAdapter adapter = BytesAdapter.of(order);
                BytesReader reader = new BytesReader(channel);

                Compressor compressor = Compressors.getInstance()
                        .compressorFor(Compression.getRequired(ifd));

                ImageDimensions imageDimensions = ImageDimensions.from(ifd);
                StripInfo stripInfo = StripInfo.from(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                byte[][] bytes = new byte[intImageDimensions.imageLength()][intImageDimensions.imageWidth()];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int imageWidth = intImageDimensions.imageWidth();
                int widthBytes = imageWidth * componentsPerPixel;

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
                    }
                }

                return new Bytes(bytes, componentsPerPixel);
            }
        }

        record ByteTiles(int componentsPerPixel) implements Reader {

            @Override
            public Bytes readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                return new Bytes(new byte[0][0], 1);
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
                StripInfo stripInfo = StripInfo.from(ifd);

                ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
                StripInfo.Int intStripInfo = stripInfo.asIntInfo();

                float[][] floats = new float[intImageDimensions.imageLength()][intImageDimensions.imageWidth()];

                int nOffsets = stripInfo.stripOffsets().length;
                int rowsPerStrip = intStripInfo.rowsPerStrip();

                int imageWidth = intImageDimensions.imageWidth();
                int widthBytes = imageWidth * BYTES_PER_FLOAT * componentsPerPixel;

                for (int i = 0; i < nOffsets; i++) {

                    long stripOffset = stripInfo.stripOffsets()[i];
                    int stripBytes = intStripInfo.stripByteCounts()[i];

                    ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                    byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                    int rowsInStrip = uncompressedStrip.length / imageWidth / BYTES_PER_FLOAT / componentsPerPixel;
                    if (i != nOffsets - 1) {
                        checkArgument(rowsInStrip == rowsPerStrip,
                                "Incorrect number of rows found (%s) in strip# (%s).", rowsInStrip, i);
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
            @Override
            public Floats readRaster(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
                return new Floats(new float[0][0], 1);
            }
        }
    }
}
