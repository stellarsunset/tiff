package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.PaletteColorImage;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class ColorMap implements Tag.Value {

    public static final Tag TAG = new Tag((short) 0x140, "COLOR_MAP");

    private final Rgb[] rgbValues;

    private ColorMap(Rgb[] rgbValues) {
        this.rgbValues = requireNonNull(rgbValues);
    }

    public static ColorMap get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<ColorMap> getIfPresent(Ifd ifd) {
        Ifd.Entry entry = ifd.findTag(TAG.id());
        return switch (entry) {
            case Entry.Short s -> Optional.of(ColorMap.create(s.values()));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(TAG, entry.getClass());
        };
    }

    /**
     * Create a new {@link ColorMap} from an array of provided shorts.
     *
     * <p>This is mostly used for testing, applications should expect to go through {@link #get(Ifd)} etc. in
     * most normal cases to retrieve the map from an IFD.
     */
    public static ColorMap create(short[] shorts) {
        int n = shorts.length / 3;
        Rgb[] rgbValues = new Rgb[n];

        int r = 0;
        int g = n;
        int b = n + n;

        while (r < n) {
            rgbValues[r] = new Rgb(
                    shorts[r],
                    shorts[g],
                    shorts[b]
            );
            r++;
            g++;
            b++;
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
     * Inverse operation to {@link #create(short[])}, generate the {@code short[]} from the ColorMap that when passed to
     * create would re-create this exact ColorMap.
     */
    public short[] flatten() {
        int n = rgbValues.length;

        int r = 0;
        int g = n;
        int b = n + n;

        short[] shorts = new short[n * 3];

        while (r < n) {
            ColorMap.Rgb rgb = rgb(r);
            shorts[r] = rgb.r();
            shorts[g] = rgb.g();
            shorts[b] = rgb.b();
            r++;
            g++;
            b++;
        }

        return shorts;
    }

    /**
     * ColorMap RGB values are 16-bit unsigned shorts instead of the 8-bit unsigned bytes used in standard RGB images.
     */
    public record Rgb(short r, short g, short b) {
    }
}
