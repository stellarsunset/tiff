package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a baseline RGB full-color image.
 *
 * <p>RGB images are provide the same coloring components as {@link PaletteColorImage}s, but store each component in its
 * own byte under-the-hood. This means they're typically three times as large, but they support far more tones.
 */
public record RgbImage(ImageDimensions dimensions, StripInfo stripInfo, Resolution resolution,
                       byte[][] data) implements BaselineImage {

    /**
     * There are three components per pixel in the underlying image, each is 8 bits (1 byte).
     *
     * <p>In the {@link Ifd} this is given by {@link SamplesPerPixel}.
     */
    private static final int SAMPLES_PER_PIXEL = 3;

    public RgbImage {
        dimensions.checkBounds(data, 3);
    }

    static Maker maker(BytesAdapter adapter) {
        return new Maker(adapter);
    }

    @Override
    public Pixel.Rgb valueAt(int row, int col) {
        int offset = col * SAMPLES_PER_PIXEL;
        byte r = data[row][offset];
        byte g = data[row][offset + 1];
        byte b = data[row][offset + 2];
        return new Pixel.Rgb(r, g, b);
    }

    record Maker(BytesAdapter adapter) implements Image.Maker {

        @Override
        public RgbImage makeImage(SeekableByteChannel channel, Ifd ifd) {

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
