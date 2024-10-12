package com.stellarsunset.terrain.tiff.image;

import com.stellarsunset.terrain.tiff.BytesAdapter;
import com.stellarsunset.terrain.tiff.BytesReader;
import com.stellarsunset.terrain.tiff.Ifd;
import com.stellarsunset.terrain.tiff.compress.Compressor;
import com.stellarsunset.terrain.tiff.compress.Compressors;
import com.stellarsunset.terrain.tiff.tag.Compression;
import com.stellarsunset.terrain.tiff.tag.SamplesPerPixel;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a baseline RGB full-color image.
 *
 * <p>By default
 */
public record RgbImage(ImageDimensions dimensions, StripInfo stripInfo, Resolution resolution,
                       byte[][] data) implements Image.Baseline {

    /**
     * There are three components per pixel in the underlying image, each is 8 bits (1 byte).
     *
     * <p>In the {@link Ifd} this is given by {@link SamplesPerPixel}.
     */
    private static final int SAMPLES_PER_PIXEL = 3;

    public RgbImage {
        checkArgument(data.length == dimensions.imageLength(),
                "Expected %s rows, found %s", dimensions.imageLength(), data.length);

        checkArgument(data[0].length / SAMPLES_PER_PIXEL == dimensions.imageWidth(),
                "Expected %s columns, found %s", dimensions.imageWidth(), data[0].length / SAMPLES_PER_PIXEL);
    }

    static Maker maker(BytesAdapter adapter) {
        return new Maker(adapter);
    }

    @Override
    public PixelValue valueAt(int row, int col) {
        int offset = col * SAMPLES_PER_PIXEL;
        byte r = data[row][offset];
        byte g = data[row][offset + 1];
        byte b = data[row][offset + 2];
        return new PixelValue.Empty();
    }

    record Maker(BytesAdapter adapter) implements ImageMaker {

        @Override
        public Image makeImage(SeekableByteChannel channel, Ifd ifd) {

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

            for (int i = 0; i < nOffsets; i++) {

                long stripOffset = stripInfo.stripOffsets()[i];
                int stripBytes = intStripInfo.stripByteCounts()[i];

                ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                int rowsInStrip = uncompressedStrip.length / SAMPLES_PER_PIXEL / imageWidth;
                if (i != nOffsets - 1) {
                    checkArgument(rowsInStrip == rowsPerStrip,
                            "Incorrect number of rows found (%s) in strip# (%s).", rowsInStrip, i);
                }

                for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                    int imageRow = i * rowsPerStrip + stripRow;
                    int stripRowStart = stripRow * imageWidth * SAMPLES_PER_PIXEL;

                    bytes[imageRow] = Arrays.copyOfRange(uncompressedStrip, stripRowStart, stripRowStart + (imageWidth * 3));
                }
            }

            return new RgbImage(
                    imageDimensions,
                    stripInfo,
                    Resolution.from(ifd),
                    bytes
            );
        }
    }
}
