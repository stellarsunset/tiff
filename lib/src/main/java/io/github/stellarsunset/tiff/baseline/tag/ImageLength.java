package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

import java.util.OptionalLong;

/**
 * The number of rows of pixels in the image.
 *
 * <p>N = 1. Type = {@link Entry.Short} | {@link Entry.Long}.
 */
public final class ImageLength implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x101, "IMAGE_LENGTH");

    public static long get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalLong getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUInt(TAG, ifd);
    }
}
