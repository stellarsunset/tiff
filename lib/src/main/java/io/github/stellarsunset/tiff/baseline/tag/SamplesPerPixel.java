package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;

import java.util.OptionalInt;

/**
 * The number of components per pixel. This number is 3 for RGB images, unless extra samples are present.
 *
 * <p>See the ExtraSamples field for further information.
 */
public final class SamplesPerPixel implements Tag.Value {

    public static final Tag TAG = new Tag((short) 0x115, "SAMPLES_PER_PIXEL");

    public static int get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalInt getIfPresent(Ifd ifd) {
        return Tag.Value.optionalUShort(TAG, ifd);
    }
}
