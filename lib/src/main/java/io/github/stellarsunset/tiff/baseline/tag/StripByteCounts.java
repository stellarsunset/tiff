package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

import java.util.Optional;

/**
 * For each strip, the number of bytes in the strip after compression.
 *
 * <p>N = StripsPerImage for PlanarConfiguration equal to 1 | SamplesPerPixel * StripsPerImage for PlanarConfiguration
 * equal to 2. Type = {@link Entry.Short} | {@link Entry.Long}.
 */
public final class StripByteCounts implements Tag.Value {

    public static final Tag TAG = new Tag((short) 0x117, "STRIP_BYTE_COUNTS");

    public static long[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<long[]> getIfPresent(Ifd ifd) {
        return Tag.Value.optionalUIntArray(TAG, ifd);
    }
}
