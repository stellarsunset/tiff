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
 * An image containing 16-bit integer data.
 *
 * <p>{@link Short1Image}s are often used to store a vertical elevation at each pixel in GeoTIFF, however multi-band
 * images may contain multiple values per pixel (e.g. {@link Short3Image} and {@link ShortNImage}).
 *
 * <p>Note the {@link ShortNImage} has difference performance characteristics from 1 and 3 component images because of
 * the extra array copy.
 *
 * <p>See also {@link IntImage} and {@link FloatImage}.
 */
public sealed interface ShortImage extends DataImage {

    static Image.Maker maker() {
        return new Maker();
    }

    @Override
    Pixel valueAt(int row, int col);

    sealed interface Pixel extends DataImage.Pixel {
    }

    record Short1Image(ImageDimensions dimensions, short[][] data) implements ShortImage {

        public Short1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel(data[row][col]);
        }

        /**
         * Represents the value of a pixel as a single 16-bit integer.
         *
         * <p>These are typically used in extensions for storing measurements (e.g. elevations) taken on grids as images.
         */
        public record Pixel(short value) implements ShortImage.Pixel {

            public int unsignedValue() {
                return java.lang.Short.toUnsignedInt(value);
            }
        }
    }

    record Short3Image(ImageDimensions dimensions, short[][] data) implements ShortImage {

        public Short3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            int offset = col * 3;
            short s1 = data[row][offset];
            short s2 = data[row][offset + 1];
            short s3 = data[row][offset + 2];
            return new Pixel(s1, s2, s3);
        }

        /**
         * Represents the value of a pixel as a trio of 16-bit integer values.
         */
        public record Pixel(short s1, short s2, short s3) implements ShortImage.Pixel {

            public int unsignedS1() {
                return java.lang.Short.toUnsignedInt(s1);
            }

            public int unsignedS2() {
                return java.lang.Short.toUnsignedInt(s2);
            }

            public int unsignedS3() {
                return java.lang.Short.toUnsignedInt(s3);
            }
        }
    }

    record ShortNImage(ImageDimensions dimensions, int componentsPerPixel, short[][] data) implements ShortImage {

        public ShortNImage {
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
         * Represents the value of a pixel as an arbitrary number of 16-bit integer values.
         */
        public record Pixel(short[] values) implements ShortImage.Pixel {
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public ShortImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.get(ifd);

            Raster.Shorts shorts = Raster.Reader.shorts(componentsPerPixel).readRaster(
                    channel,
                    order,
                    ifd
            );

            ImageDimensions dimensions = ImageDimensions.get(ifd);

            return switch (componentsPerPixel) {
                case 1 -> new Short1Image(dimensions, shorts.shorts());
                case 3 -> new Short3Image(dimensions, shorts.shorts());
                default -> new ShortNImage(dimensions, componentsPerPixel, shorts.shorts());
            };
        }
    }
}
