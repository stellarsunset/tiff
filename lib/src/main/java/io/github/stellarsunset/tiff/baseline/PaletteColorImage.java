package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.tag.ColorMap;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

/**
 * Palette-color images are similar to grayscale images. They still have one component per pixel, but the component value
 * is used as an index into a full RGB-lookup table, i.e. a {@link ColorMap}.
 *
 * <p>See {@link Pixel} for more info.
 */
public record PaletteColorImage(ImageDimensions dimensions, Resolution resolution, ColorMap colorMap,
                                byte[][] data) implements BaselineImage {

    public PaletteColorImage {
        dimensions.checkBounds(data, 1);
    }

    static Maker maker() {
        return new Maker();
    }

    @Override
    public Pixel valueAt(int row, int col) {

        byte index = data[row][col];

        ColorMap.Rgb rgb = colorMap.rgb(Byte.toUnsignedInt(index));

        return new Pixel(
                index,
                rgb.r(),
                rgb.g(),
                rgb.b()
        );
    }

    /**
     * Palette color image pixels are a byte value that can be used to index into a {@link ColorMap}, the color map then
     * contains the {@link RgbImage.Pixel} value of the pixel.
     *
     * <p>I.e. it's a way to encode a limited set of RGB values compactly. These colors can also have more gradation to
     * them as each component (R, G, or B) is 16 bits instead of 8 as in a standard {@link RgbImage}.
     *
     * <p>Remember these values are unsigned, so to compare actual values apply the correct unsigned Java conversion.
     *
     * @param index the index in the {@link ColorMap} of the pixel value
     * @param r     the red component from the {@link ColorMap}
     * @param g     the green component from the {@link ColorMap}
     * @param b     the blue component from the {@link ColorMap}
     */
    public record Pixel(byte index, short r, short g, short b) implements BaselineImage.Pixel {

        public int unsignedIndex() {
            return java.lang.Byte.toUnsignedInt(index);
        }

        public int unsignedR() {
            return java.lang.Short.toUnsignedInt(r);
        }

        public int unsignedG() {
            return java.lang.Short.toUnsignedInt(g);
        }

        public int unsignedB() {
            return java.lang.Short.toUnsignedInt(b);
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public PaletteColorImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            Raster.Bytes bytes = Raster.Reader.bytes(1).readRaster(
                    channel,
                    order,
                    ifd
            );

            return new PaletteColorImage(
                    ImageDimensions.get(ifd),
                    Resolution.from(ifd),
                    ColorMap.get(ifd),
                    bytes.bytes()
            );
        }
    }
}
