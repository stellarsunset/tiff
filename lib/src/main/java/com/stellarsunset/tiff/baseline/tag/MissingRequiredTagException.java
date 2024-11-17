package com.stellarsunset.tiff.baseline.tag;

/**
 * Custom {@link RuntimeException} for use when a required tag is missing from the underlying TIFF file and is needed for
 * proper parsing of the file.
 */
public final class MissingRequiredTagException extends RuntimeException {

    public MissingRequiredTagException(String name, short id) {
        super(String.format("Missing required tag with ID: %d. User-friendly name: %s", Short.toUnsignedInt(id), name));
    }
}
