package com.stellarsunset.terrain.tiff.tag;

public final class UnsupportedTypeForTagException extends RuntimeException {

    UnsupportedTypeForTagException(String name, short id) {
        super(String.format("Incorrect type encountered for tag with ID: %d. User-friendly name: %s", Short.toUnsignedInt(id), name));
    }
}
