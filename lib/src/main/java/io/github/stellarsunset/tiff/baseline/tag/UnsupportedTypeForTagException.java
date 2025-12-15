package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;

public final class UnsupportedTypeForTagException extends RuntimeException {

    public UnsupportedTypeForTagException(String name, short id, Class<? extends Ifd.Entry> type) {
        super(String.format("Incorrect type: %s, encountered for tag with ID: %d. User-friendly name: %s.", type.getSimpleName(), Short.toUnsignedInt(id), name));
    }

    public UnsupportedTypeForTagException(Tag tag, Class<? extends Ifd.Entry> type) {
        this(tag.name(), tag.id(), type);
    }
}
