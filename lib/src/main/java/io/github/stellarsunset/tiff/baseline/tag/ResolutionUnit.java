package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

import java.util.OptionalInt;

/**
 * The unit of measurement for {@link XResolution} and {@link YResolution}.
 *
 * <p>N = 1. Type = {@link Entry.Short}. Default = 2.
 * <ul>
 *     <li>1 = No absolute unit of measurement. Used for images that may have a non-square aspect ratio, but no
 *     meaningful absolute dimensions.</li>
 *     <li>2 = Inch.</li>
 *     <li>3 = Centimeter.</li>
 * </ul>
 */
public final class ResolutionUnit implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x128, "RESOLUTION_UNIT");

    /**
     * Unless otherwise specified the resolution units of the TIFF file are in inches, therefore this always returns a
     * value.
     */
    public static int get(Ifd ifd) {
        OptionalInt maybeRes = Tag.Accessor.optionalUShort(TAG, ifd);
        return maybeRes.isPresent() ? maybeRes.getAsInt() : 2;
    }
}
