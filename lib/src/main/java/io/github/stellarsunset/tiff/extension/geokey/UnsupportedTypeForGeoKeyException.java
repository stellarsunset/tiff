package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

/**
 * Identical to {@link UnsupportedTypeForTagException} but scoped to GeoKeys.
 */
public final class UnsupportedTypeForGeoKeyException extends RuntimeException {

    public UnsupportedTypeForGeoKeyException(String name, short id, Class<? extends Ifd.Entry> type) {
        super(String.format("Incorrect type: %s, encountered for key with ID: %d. User-friendly name: %s.", type.getSimpleName(), Short.toUnsignedInt(id), name));
    }

    public UnsupportedTypeForGeoKeyException(GeoKey key, Class<? extends Ifd.Entry> type) {
        this(key.name(), key.id(), type);
    }
}
