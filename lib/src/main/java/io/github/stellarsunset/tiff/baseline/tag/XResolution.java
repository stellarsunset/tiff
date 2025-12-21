package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Rational;
import io.github.stellarsunset.tiff.Tag;

import java.util.Optional;

/**
 * The number of pixels per ResolutionUnit in the {@link ImageWidth} direction.
 *
 * <p>N = 1. Type = {@link Entry.Rational}.
 */
public final class XResolution implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x11A, "X_RESOLUTION");

    public static Rational get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<Rational> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalRational(TAG, ifd);
    }
}
