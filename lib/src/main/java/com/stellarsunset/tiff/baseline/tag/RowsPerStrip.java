package com.stellarsunset.tiff.baseline.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;

import java.util.OptionalLong;

/**
 * The number of rows per strip.
 *
 * <p>N = 1. Type = {@link Entry.Short} | {@link Entry.Long}.
 *
 * <p>TIFF image data is organized into strips for faster random access and efficient I/O buffering.
 *
 * <p>RowsPerStrip and ImageLength together tell us the number of strips in the entire image. The equation is:
 * {@code StripsPerImage = floor ((ImageLength + RowsPerStrip - 1) / RowsPerStrip)}
 */
public final class RowsPerStrip {

    public static final String NAME = "ROWS_PER_STRIP";

    public static final short ID = 0x116;

    public static long getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static OptionalLong getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> OptionalLong.of(Short.toUnsignedLong(s.values()[0]));
            case Entry.Long l -> OptionalLong.of(Integer.toUnsignedLong(l.values()[0]));
            case Entry.NotFound _ -> OptionalLong.of(2);
            case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _,
                    Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
