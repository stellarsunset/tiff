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

    Pixel.Short valueAt(int row, int col);

    record Short1Image(ImageDimensions dimensions, short[][] data) implements ShortImage {

        public Short1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel.Short1 valueAt(int row, int col) {
            return new Pixel.Short1(data[row][col]);
        }
    }

    record Short3Image(ImageDimensions dimensions, short[][] data) implements ShortImage {

        public Short3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel.Short3 valueAt(int row, int col) {
            int offset = col * 3;
            short s1 = data[row][offset];
            short s2 = data[row][offset + 1];
            short s3 = data[row][offset + 2];
            return new Pixel.Short3(s1, s2, s3);
        }
    }

    record ShortNImage(ImageDimensions dimensions, int componentsPerPixel, short[][] data) implements ShortImage {

        public ShortNImage {
            dimensions.checkBounds(data, componentsPerPixel);
        }

        @Override
        public Pixel.ShortN valueAt(int row, int col) {
            int offset = col * componentsPerPixel;
            return new Pixel.ShortN(
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
        public ShortImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.getRequired(ifd);

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
