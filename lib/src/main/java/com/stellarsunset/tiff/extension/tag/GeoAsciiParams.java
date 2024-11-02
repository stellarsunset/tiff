package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.util.Optional;

public final class GeoAsciiParams {

    public static final String NAME = "GEO_ASCII_PARAMS";

    public static final short ID = (short) 0x34737;

    public static byte[] getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<byte[]> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Ascii d -> Optional.of(d.values());
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
