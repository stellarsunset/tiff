package com.stellarsunset.tiff.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;

import java.util.OptionalInt;

/**
 * The unit of measurement for {@link XResolution} and {@link YResolution}.
 *
 * <p>N = 1. Type = {@link Entry.Short}. Default = 2.
 * <ul>
 *     <li>1 = No absolute unit of measurement. Used for images that may have a non-square aspect ratio, but no
 *     meaningful absolute dimensions.</li>
 *     <li>2 = Inch.</li>
 *     <li>3 = Centimeter.</li>
 * </ul>
 */
public final class ResolutionUnit {

    public static final String NAME = "RESOLUTION_UNIT";

    public static final short ID = 0x128;

    public static int getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static OptionalInt getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> OptionalInt.of(Short.toUnsignedInt(s.values()[0]));
            case Entry.NotFound _ -> OptionalInt.of(2);
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _,
                    Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
