package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import com.stellarsunset.tiff.extension.geo.GeoKeyDirectory;

import java.util.Optional;

/**
 * A top-level TIFF tag used to store ASCII values associated with {@link GeoKeyDirectory} entry values.
 */
public final class GeoDoubleParams {

    public static final String NAME = "GEO_DOUBLE_PARAMS";

    public static final short ID = (short) 0x34736;

    public static double[] getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<double[]> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Double d -> Optional.of(d.values());
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                 Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}

