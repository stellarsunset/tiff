package com.stellarsunset.tiff.baseline.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;

import java.util.Optional;

/**
 * For each strip, the number of bytes in the strip after compression.
 *
 * <p>N = StripsPerImage for PlanarConfiguration equal to 1 | SamplesPerPixel * StripsPerImage for PlanarConfiguration
 * equal to 2. Type = {@link Entry.Short} | {@link Entry.Long}.
 */
public final class StripByteCounts {

    public static final String NAME = "STRIP_BYTE_COUNTS";

    public static final short ID = 0x117;

    public static long[] getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<long[]> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Optional.of(Arrays.toUnsignedLongArray(s.values()));
            case Entry.Long l -> Optional.of(Arrays.toUnsignedLongArray(l.values()));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _,
                    Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
