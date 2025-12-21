package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

/**
 * Custom {@link RuntimeException} for use when a required GeoKey is missing from the underlying TIFF file and is needed
 * for proper interpretation of the geo image data in the file.
 *
 * <p>Identical to {@link MissingRequiredTagException}, but scoped to GeoKeys in GeoTIFFs.
 */
public final class MissingRequiredGeoKeyException extends RuntimeException {

    public MissingRequiredGeoKeyException(String name, short id) {
        super(String.format("Missing required GeoKey with ID: %d. User-friendly name: %s", Short.toUnsignedInt(id), name));
    }

    public MissingRequiredGeoKeyException(GeoKey key) {
        this(key.name(), key.id());
    }
}
