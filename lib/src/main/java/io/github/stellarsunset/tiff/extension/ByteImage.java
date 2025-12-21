package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.BaselineImage;
import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

/**
 * An image containing 8-bit byte data.
 *
 * <p>All {@link BaselineImage} types can also have their image content read as a {@link ByteImage} since fundamentally
 * the image data itself is encoded as {@link Raster}s of bytes in them.
 *
 * <p>This is provided primarily for completeness and if for whatever reason the client wants to opt-out of the semantic
 * flavor the builtin baseline image handles provide.
 *
 * <p>See also {@link ShortImage}, {@link IntImage}, and {@link FloatImage}.
 */
public sealed interface ByteImage extends DataImage {

    static Image.Maker maker() {
        return new Maker();
    }

    @Override
    Pixel valueAt(int row, int col);

    sealed interface Pixel extends DataImage.Pixel {
    }

    record Byte1Image(ImageDimensions dimensions, byte[][] data) implements ByteImage {

        public Byte1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel(data[row][col]);
        }

        /**
         * Represents the value of a pixel as a single 8-bit integer (byte).
         *
         * <p>Most {@link BaselineImage.Pixel} types can be represented a {@link Byte1Image.Pixel} or {@link Byte3Image.Pixel}
         * types.
         */
        public record Pixel(byte value) implements ByteImage.Pixel {

            public int unsignedValue() {
                return java.lang.Byte.toUnsignedInt(value);
            }
        }
    }

    record Byte3Image(ImageDimensions dimensions, byte[][] data) implements ByteImage {

        public Byte3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            int offset = col * 3;
            byte b1 = data[row][offset];
            byte b2 = data[row][offset + 1];
            byte b3 = data[row][offset + 2];
            return new Pixel(b1, b2, b3);
        }

        /**
         * Represents the value of a pixel as a trio of 8-bit integer (byte) values.
         */
        public record Pixel(byte b1, byte b2, byte b3) implements ByteImage.Pixel {

            public int unsignedB1() {
                return java.lang.Byte.toUnsignedInt(b1);
            }

            public int unsignedB2() {
                return java.lang.Byte.toUnsignedInt(b2);
            }

            public int unsignedB3() {
                return java.lang.Byte.toUnsignedInt(b3);
            }
        }
    }

    record ByteNImage(ImageDimensions dimensions, int componentsPerPixel, byte[][] data) implements ByteImage {

        public ByteNImage {
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
         * Represents the value of a pixel as an arbitrary number of 8-bit integer (byte) values.
         */
        public record Pixel(byte[] values) implements ByteImage.Pixel {
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public ByteImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.get(ifd);

            Raster.Bytes bytes = Raster.Reader.bytes(componentsPerPixel).readRaster(
                    channel,
                    order,
                    ifd
            );

            ImageDimensions dimensions = ImageDimensions.get(ifd);

            return switch (componentsPerPixel) {
                case 1 -> new Byte1Image(dimensions, bytes.bytes());
                case 3 -> new Byte3Image(dimensions, bytes.bytes());
                default -> new ByteNImage(dimensions, componentsPerPixel, bytes.bytes());
            };
        }
    }
}
