package com.stellarsunset.tiff.baseline.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;

import java.util.OptionalInt;

/**
 * The number of components per pixel. This number is 3 for RGB images, unless extra samples are present.
 *
 * <p>See the ExtraSamples field for further information.
 */
public final class SamplesPerPixel {

    public static final String NAME = "SAMPLES_PER_PIXEL";

    public static final short ID = 0x115;

    public static int getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static OptionalInt getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> OptionalInt.of(Short.toUnsignedInt(s.values()[0]));
            case Entry.NotFound _ -> OptionalInt.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
