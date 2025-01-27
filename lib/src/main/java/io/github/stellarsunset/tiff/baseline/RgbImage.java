package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Pixel;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

/**
 * Represents a baseline RGB full-color image.
 *
 * <p>RGB images are provide the same coloring components as {@link PaletteColorImage}s, but store each component in its
 * own byte under-the-hood. This means they're typically three times as large, but they support far more tones.
 */
public record RgbImage(ImageDimensions dimensions, Resolution resolution, byte[][] data) implements BaselineImage {

    /**
     * There are three components per pixel in the underlying image, each is 8 bits (1 byte).
     *
     * <p>In the {@link Ifd} this is given by {@link SamplesPerPixel}.
     */
    private static final int SAMPLES_PER_PIXEL = 3;

    public RgbImage {
        dimensions.checkBounds(data, 3);
    }

    static Maker maker() {
        return new Maker();
    }

    @Override
    public Pixel.Rgb valueAt(int row, int col) {
        int offset = col * SAMPLES_PER_PIXEL;
        byte r = data[row][offset];
        byte g = data[row][offset + 1];
        byte b = data[row][offset + 2];
        return new Pixel.Rgb(r, g, b);
    }

    record Maker() implements Image.Maker {

        @Override
        public RgbImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            Raster.Bytes bytes = Raster.Reader.bytes(SAMPLES_PER_PIXEL).readRaster(
                    channel,
                    order,
                    ifd
            );

            return new RgbImage(ImageDimensions.get(ifd), Resolution.from(ifd), bytes.bytes());
        }
    }
}
