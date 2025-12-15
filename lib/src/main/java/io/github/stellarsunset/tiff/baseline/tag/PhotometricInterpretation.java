package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

import java.util.OptionalInt;

/**
 * The color space of the image data. Refer to the TIFF file docs for specific code meanings.
 *
 * <p>N = 1. Type = {@link Entry.Short}.
 */
public final class PhotometricInterpretation implements Tag.Value {

    public static final Tag TAG = new Tag((short) 0x106, "PHOTOMETRIC_INTERPRETATION");

    public static int get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalInt getIfPresent(Ifd ifd) {
        return Tag.Value.optionalUShort(TAG, ifd);
    }
}
