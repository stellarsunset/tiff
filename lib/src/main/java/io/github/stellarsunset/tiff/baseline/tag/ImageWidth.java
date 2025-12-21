package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

import java.util.OptionalLong;

/**
 * The number of columns in the image, i.e., the number of pixels per row.
 *
 * <p>N = 1. Type = {@link Entry.Short} | {@link Entry.Long}.
 */
public final class ImageWidth implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x100, "IMAGE_WIDTH");

    public static long get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalLong getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUInt(TAG, ifd);
    }
}
