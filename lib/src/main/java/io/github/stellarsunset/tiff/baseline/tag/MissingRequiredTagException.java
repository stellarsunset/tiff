package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Tag;

/**
 * Custom {@link RuntimeException} for use when a required tag is missing from the underlying TIFF file and is needed for
 * proper parsing of the file.
 */
public final class MissingRequiredTagException extends RuntimeException {

    public MissingRequiredTagException(String name, short id) {
        super(String.format("Missing required tag with ID: %d. User-friendly name: %s", Short.toUnsignedInt(id), name));
    }

    public MissingRequiredTagException(Tag tag) {
        this(tag.name(), tag.id());
    }
}
