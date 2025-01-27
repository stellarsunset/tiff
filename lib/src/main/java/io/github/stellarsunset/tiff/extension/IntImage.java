package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Pixel;
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

    Pixel.Int valueAt(int row, int col);

    record Int1Image(ImageDimensions dimensions, int[][] data) implements IntImage {

        public Int1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel.Int1 valueAt(int row, int col) {
            return new Pixel.Int1(data[row][col]);
        }
    }

    record Int3Image(ImageDimensions dimensions, int[][] data) implements IntImage {

        public Int3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel.Int3 valueAt(int row, int col) {
            int offset = col * 3;
            int i1 = data[row][offset];
            int i2 = data[row][offset + 1];
            int i3 = data[row][offset + 2];
            return new Pixel.Int3(i1, i2, i3);
        }
    }

    record IntNImage(ImageDimensions dimensions, int componentsPerPixel, int[][] data) implements IntImage {

        public IntNImage {
            dimensions.checkBounds(data, componentsPerPixel);
        }

        @Override
        public Pixel.IntN valueAt(int row, int col) {
            int offset = col * componentsPerPixel;
            return new Pixel.IntN(
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
        public IntImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.getRequired(ifd);

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
