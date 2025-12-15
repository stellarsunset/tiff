package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.compress.Compressors;

/**
 * Compression scheme used on the image data.
 *
 * <p>N = 1. Type = {@link Entry.Short}.
 */
public final class Compression implements Tag.Value {

    public static final Tag TAG = new Tag((short) 0x103, "COMPRESSION");

    /**
     * Used with the {@link Compressors} registry of compression algorithms to pack/unpack the bytes of an image.
     */
    public static int get(Ifd ifd) {
        return Tag.Value.optionalUShort(TAG, ifd).orElse(1);
    }
}
