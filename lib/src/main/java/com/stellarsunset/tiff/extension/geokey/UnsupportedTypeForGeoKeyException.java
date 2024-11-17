package com.stellarsunset.tiff.extension.geokey;

import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

/**
 * Identical to {@link UnsupportedTypeForTagException} but scoped to GeoKeys.
 */
public final class UnsupportedTypeForGeoKeyException extends RuntimeException {

    public UnsupportedTypeForGeoKeyException(String name, short id) {
        super(String.format("Incorrect type encountered for GeoKey with ID: %d. User-friendly name: %s", Short.toUnsignedInt(id), name));
    }
}
