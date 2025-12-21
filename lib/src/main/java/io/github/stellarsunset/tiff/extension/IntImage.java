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
 * An image containing 32-bit integer data.
 *
 * <p>{@link Int1Image}s are often used to store a vertical elevation at each pixel in GeoTIFF, however multi-band images
 * may contain multiple values per pixel (e.g. {@link Int3Image} and {@link IntNImage}).
 *
 * <p>Note the {@link IntNImage} has difference performance characteristics from 1 and 3 component images because of the
 * extra array copy.
 *
 * <p>See also {@link ShortImage} and {@link FloatImage}.
 */
public sealed interface IntImage extends DataImage {

    static Image.Maker maker() {
        return new Maker();
    }

    @Override
    Pixel valueAt(int row, int col);

    sealed interface Pixel extends DataImage.Pixel {
    }

    record Int1Image(ImageDimensions dimensions, int[][] data) implements IntImage {

        public Int1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel(data[row][col]);
        }

        /**
         * Represents the value of a pixel as a single 32-bit integer.
         *
         * <p>These are typically used in extensions for storing measurements (e.g. elevations) taken on grids as images.
         */
        public record Pixel(int value) implements IntImage.Pixel {

            public long unsignedValue() {
                return Integer.toUnsignedLong(value);
            }
        }
    }

    record Int3Image(ImageDimensions dimensions, int[][] data) implements IntImage {

        public Int3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            int offset = col * 3;
            int i1 = data[row][offset];
            int i2 = data[row][offset + 1];
            int i3 = data[row][offset + 2];
            return new Pixel(i1, i2, i3);
        }

        /**
         * Represents the value of a pixel as a trio of 32-bit integer values.
         */
        public record Pixel(int i1, int i2, int i3) implements IntImage.Pixel {

            public long unsignedI1() {
                return java.lang.Integer.toUnsignedLong(i1);
            }

            public long unsignedI2() {
                return java.lang.Integer.toUnsignedLong(i2);
            }

            public long unsignedI3() {
                return java.lang.Integer.toUnsignedLong(i3);
            }
        }
    }

    record IntNImage(ImageDimensions dimensions, int componentsPerPixel, int[][] data) implements IntImage {

        public IntNImage {
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
         * Represents the value of a pixel as an arbitrary number of 32-bit integer values.
         */
        public record Pixel(int[] values) implements IntImage.Pixel {
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public IntImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.get(ifd);

            Raster.Ints ints = Raster.Reader.ints(componentsPerPixel).readRaster(
                    channel,
                    order,
                    ifd
            );

            ImageDimensions dimensions = ImageDimensions.get(ifd);

            return switch (componentsPerPixel) {
                case 1 -> new Int1Image(dimensions, ints.ints());
                case 3 -> new Int3Image(dimensions, ints.ints());
                default -> new IntNImage(dimensions, componentsPerPixel, ints.ints());
            };
        }
    }
}
