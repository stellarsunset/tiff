package io.github.stellarsunset.tiff.baseline.tag;

public final class UnsupportedTypeForTagException extends RuntimeException {

    public UnsupportedTypeForTagException(String name, short id) {
        super(String.format("Incorrect type encountered for tag with ID: %d. User-friendly name: %s", Short.toUnsignedInt(id), name));
    }
}
