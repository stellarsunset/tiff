package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Rational;

import java.util.Optional;

/**
 * The number of pixels per ResolutionUnit in the {@link ImageLength} direction.
 *
 * <p>N = 1. Type = {@link Entry.Rational}.
 */
public final class YResolution {

    public static final String NAME = "Y_RESOLUTION";

    public static final short ID = 0x11B;

    public static Rational getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<Rational> getOptional(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Rational r -> Optional.of(r.rational(0));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.SByte _, Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _,
                    Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
