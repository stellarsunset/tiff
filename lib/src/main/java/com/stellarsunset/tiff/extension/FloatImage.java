package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.Pixel;
import com.stellarsunset.tiff.Raster;
import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.tag.SamplesPerPixel;

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

    Pixel.Float valueAt(int row, int col);

    record Float1Image(ImageDimensions dimensions, float[][] data) implements FloatImage {

        public Float1Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel.Float1 valueAt(int row, int col) {
            return new Pixel.Float1(data[row][col]);
        }
    }

    record Float3Image(ImageDimensions dimensions, float[][] data) implements FloatImage {

        public Float3Image {
            dimensions.checkBounds(data, 3);
        }

        @Override
        public Pixel.Float3 valueAt(int row, int col) {
            int offset = col * 3;
            float f1 = data[row][offset];
            float f2 = data[row][offset + 1];
            float f3 = data[row][offset + 2];
            return new Pixel.Float3(f1, f2, f3);
        }
    }

    record FloatNImage(ImageDimensions dimensions, int componentsPerPixel, float[][] data) implements FloatImage {

        public FloatNImage {
            dimensions.checkBounds(data, componentsPerPixel);
        }

        @Override
        public Pixel.FloatN valueAt(int row, int col) {
            int offset = col * componentsPerPixel;
            return new Pixel.FloatN(
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
        public FloatImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int componentsPerPixel = SamplesPerPixel.getRequired(ifd);

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
