package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Pixel;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.tag.ColorMap;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

/**
 * Palette-color images are similar to grayscale images. They still have one component per pixel, but the component value
 * is used as an index into a full RGB-lookup table, i.e. a {@link ColorMap}.
 *
 * <p>See {@link Pixel.PaletteColor} for more info.
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
    public Pixel.PaletteColor valueAt(int row, int col) {

        byte index = data[row][col];

        ColorMap.Rgb rgb = colorMap.rgb(Byte.toUnsignedInt(index));

        return new Pixel.PaletteColor(
                index,
                rgb.r(),
                rgb.g(),
                rgb.b()
        );
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
                    ColorMap.getRequired(ifd),
                    bytes.bytes()
            );
        }
    }
}
