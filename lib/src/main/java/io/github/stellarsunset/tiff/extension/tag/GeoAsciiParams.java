package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Optional;

/**
 * A top-level TIFF tag used to store ASCII values associated with {@link GeoKeyDirectory} entry values.
 */
public final class GeoAsciiParams {

    public static final Tag TAG = new Tag((short) 0x87B1, "GEO_ASCII_PARAMS");

    public static byte[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<byte[]> getIfPresent(Ifd ifd) {
        return Tag.Value.optionalAsciiArray(TAG, ifd);
    }
}
