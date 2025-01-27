package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;

/**
 * Compression scheme used on the image data.
 *
 * <p>N = 1. Type = {@link Entry.Short}.
 */
public final class Compression {

    public static final String NAME = "COMPRESSION";

    public static final short ID = 0x103;

    public static int get(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Short.toUnsignedInt(s.values()[0]);
            case Entry.NotFound _ -> 1;
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
