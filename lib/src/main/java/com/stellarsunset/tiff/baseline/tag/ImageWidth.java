package com.stellarsunset.tiff.baseline.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;

import java.util.OptionalLong;

/**
 * The number of columns in the image, i.e., the number of pixels per row.
 *
 * <p>N = 1. Type = {@link Entry.Short} | {@link Entry.Long}.
 */
public final class ImageWidth {

    public static final String NAME = "IMAGE_WIDTH";

    public static final short ID = 0x100;

    public static long getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static OptionalLong getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> OptionalLong.of(Short.toUnsignedLong(s.values()[0]));
            case Entry.Long l -> OptionalLong.of(Integer.toUnsignedLong(l.values()[0]));
            case Entry.NotFound _ -> OptionalLong.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _,
                    Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}