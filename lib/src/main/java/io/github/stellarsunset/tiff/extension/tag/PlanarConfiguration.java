package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;

import java.util.OptionalInt;

/**
 * How the components of each pixel are stored.
 *
 * <p>Chunky format. The component values for each pixel are stored contiguously. The order of the components within the
 * pixel is specified by PhotometricInterpretation. For example, for RGB data, the data is stored as RGBRGBRGB…
 *
 * <p>2 = Planar format. The components are stored in separate “component planes.”
 *
 * <p>The values in StripOffsets and StripByteCounts are then arranged as a 2-dimensional array, with SamplesPerPixel
 * rows and StripsPerImage columns. All of the columns for row 0 are stored first, followed by the columns of row 1, and
 * so on.
 *
 * <p>PhotometricInterpretation describes the type of data stored in each component plane. For example, RGB data is stored
 * with the Red components in one component plane, the Green in another, and the Blue in another. PlanarConfiguration=2
 * is not currently in widespread use and it is not recommended for general interchange.
 *
 * <p>It is used as an extension and Baseline TIFF readers are not required to support it.
 *
 * <p>If SamplesPerPixel is 1, PlanarConfiguration is irrelevant, and need not be included. If a row interleave effect
 * is desired, a writer might write out the data as PlanarConfiguration=2—separate sample planes—but break up the planes
 * into multiple strips (one row per strip, perhaps) and interleave the strips.
 *
 * <p>Default is 1. See also {@link BitsPerSample}, {@link SamplesPerPixel}.
 */
public final class PlanarConfiguration implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x11C, "PLANAR_CONFIGURATION");

    public static int get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalInt getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUShort(TAG, ifd);
    }
}
