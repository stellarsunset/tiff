package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.util.Optional;

/**
 * A top-level TIFF tag used to store ASCII values associated with {@link GeoKeyDirectory} entry values.
 */
public final class GeoAsciiParams {

    public static final String NAME = "GEO_ASCII_PARAMS";

    public static final short ID = (short) 0x87B1;

    public static byte[] getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<byte[]> getOptional(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Ascii d -> Optional.of(d.values());
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
