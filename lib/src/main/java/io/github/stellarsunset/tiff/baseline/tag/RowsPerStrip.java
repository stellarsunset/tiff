package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

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
public final class RowsPerStrip implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x116, "ROWS_PER_STRIP");

    public static long get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalLong getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUInt(TAG, ifd);
    }
}
