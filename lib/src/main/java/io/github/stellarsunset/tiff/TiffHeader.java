package io.github.stellarsunset.tiff;

import java.nio.ByteOrder;

/**
 * Models the 8-byte TIFF file header as a byte order, a two byte arbitrary number, and a 4-byte pointer to the offset of
 * the first IFD in the file.
 */
public record TiffHeader(ByteOrder order, short arbitraryNumber, int firstIfdOffset) {

    public long unsignedFirstIfdOffset() {
        return Integer.toUnsignedLong(firstIfdOffset);
    }
}
