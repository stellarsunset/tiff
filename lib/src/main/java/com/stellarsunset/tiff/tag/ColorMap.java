package com.stellarsunset.tiff.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.image.PaletteColorImage;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class ColorMap {

    public static final String NAME = "COLOR_MAP";

    public static final short ID = 0x140;

    private final Rgb[] rgbValues;

    private ColorMap(Rgb[] rgbValues) {
        this.rgbValues = requireNonNull(rgbValues);
    }

    public static ColorMap getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<ColorMap> getOptional(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Optional.of(ColorMap.create(s.values()));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }

    private static ColorMap create(short[] shorts) {
        int n = shorts.length / 3;
        Rgb[] rgbValues = new Rgb[n];

        for (int i = 0; i < n; i++) {
            int offset = i * 3;
            rgbValues[i] = new Rgb(
                    shorts[offset],
                    shorts[offset + 1],
                    shorts[offset + 2]
            );
        }

        return new ColorMap(rgbValues);
    }

    /**
     * Returns the RGB value for at the provided index.
     *
     * <p>For {@link PaletteColorImage}s the index is the unsigned value of the byte of the image.
     */
    public Rgb rgb(int index) {
        return rgbValues[index];
    }

    /**
     * ColorMap RGB values are 16-bit unsigned shorts instead of the 8-bit unsigned bytes used in standard RGB images.
     */
    public record Rgb(short r, short g, short b) {
    }
}
