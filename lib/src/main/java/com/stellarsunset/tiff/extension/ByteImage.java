package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.Pixel;
import com.stellarsunset.tiff.Raster;
import com.stellarsunset.tiff.baseline.BaselineImage;
import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.tag.SamplesPerPixel;

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

    Pixel.Byte valueAt(int row, int col);

    record Byte1Image(ImageDimensions dimensions, byte[][] data) implements ByteImage {

        public Byte1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel.Byte1 valueAt(int row, int col) {
            return new Pixel.Byte1(data[row][col]);
        }
    }

    record Byte3Image(ImageDimensions dimensions, byte[][] data) implements ByteImage {

        public Byte3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel.Byte3 valueAt(int row, int col) {
            int offset = col * 3;
            byte b1 = data[row][offset];
            byte b2 = data[row][offset + 1];
            byte b3 = data[row][offset + 2];
            return new Pixel.Byte3(b1, b2, b3);
        }
    }

    record ByteNImage(ImageDimensions dimensions, int componentsPerPixel, byte[][] data) implements ByteImage {

        public ByteNImage {
            dimensions.checkBounds(data, componentsPerPixel);
        }

        @Override
        public Pixel.ByteN valueAt(int row, int col) {
            int offset = col * componentsPerPixel;
            return new Pixel.ByteN(
                    Arrays.copyOfRange(
                            data[row],
                            offset,
                            offset + componentsPerPixel
                    )
            );
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public ByteImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.getRequired(ifd);

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
