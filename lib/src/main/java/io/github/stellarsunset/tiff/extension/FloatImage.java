package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * An image containing 32-bit floating-point data.
 *
 * <p>{@link Float1Image}s are often used to store a vertical elevation at each pixel in GeoTIFF, however multi-band
 * images may contain multiple values per pixel (e.g. {@link Float3Image} and {@link FloatNImage}).
 *
 * <p>Note the {@link FloatNImage} has difference performance characteristics from 1 and 3 component images because of
 * the extra array copy.
 *
 * <p>See also {@link ShortImage} and {@link IntImage}.
 */
public sealed interface FloatImage extends DataImage {

    static Image.Maker maker() {
        return new Maker();
    }

    @Override
    Pixel valueAt(int row, int col);

    sealed interface Pixel extends DataImage.Pixel {
    }

    record Float1Image(ImageDimensions dimensions, float[][] data) implements FloatImage {

        public Float1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel(data[row][col]);
        }

        /**
         * Represents the value of a pixel as a single 32-bit float.
         *
         * <p>These are typically used in extensions for storing measurements (e.g. elevations) taken on grids as images.
         */
        public record Pixel(float value) implements FloatImage.Pixel {
        }
    }

    record Float3Image(ImageDimensions dimensions, float[][] data) implements FloatImage {

        public Float3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            int offset = col * 3;
            float f1 = data[row][offset];
            float f2 = data[row][offset + 1];
            float f3 = data[row][offset + 2];
            return new Pixel(f1, f2, f3);
        }

        /**
         * Represents the value of a pixel as a trio of 32-bit float values.
         */
        public record Pixel(float f1, float f2, float f3) implements FloatImage.Pixel {
        }
    }

    record FloatNImage(ImageDimensions dimensions, int componentsPerPixel, float[][] data) implements FloatImage {

        public FloatNImage {
            dimensions.checkBounds(data, componentsPerPixel);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            int offset = col * componentsPerPixel;
            return new Pixel(
                    Arrays.copyOfRange(
                            data[row],
                            offset,
                            offset + componentsPerPixel
                    )
            );
        }

        /**
         * Represents the value of a pixel as an arbitrary number of 32-bit float values.
         */
        public record Pixel(float[] values) implements FloatImage.Pixel {
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public FloatImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.get(ifd);

            Raster.Floats floats = Raster.Reader.floats(componentsPerPixel).readRaster(
                    channel,
                    order,
                    ifd
            );

            ImageDimensions dimensions = ImageDimensions.get(ifd);

            return switch (componentsPerPixel) {
                case 1 -> new Float1Image(dimensions, floats.floats());
                case 3 -> new Float3Image(dimensions, floats.floats());
                default -> new FloatNImage(dimensions, componentsPerPixel, floats.floats());
            };
        }
    }
}
