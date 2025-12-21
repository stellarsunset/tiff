package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Optional;

/**
 * A top-level TIFF tag used to store ASCII values associated with {@link GeoKeyDirectory} entry values.
 */
public final class GeoDoubleParams implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x87B0, "GEO_DOUBLE_PARAMS");

    public static double[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<double[]> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalDoubleArray(TAG, ifd);
    }
}

