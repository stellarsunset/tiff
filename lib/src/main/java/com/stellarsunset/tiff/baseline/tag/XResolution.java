package com.stellarsunset.tiff.baseline.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.Rational;

import java.util.Optional;

/**
 * The number of pixels per ResolutionUnit in the {@link ImageWidth} direction.
 *
 * <p>N = 1. Type = {@link Entry.Rational}.
 */
public final class XResolution {

    public static final String NAME = "X_RESOLUTION";

    public static final short ID = 0x11A;

    public static Rational getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<Rational> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Rational r -> Optional.of(r.rational(0));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.SByte _, Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _,
                    Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
