package com.stellarsunset.tiff;

import java.nio.ByteOrder;

/**
 * Models the 8-byte TIFF file header as a byte order, a two byte arbitrary number, and a 4-byte pointer to the offset of
 * the first IFD in the file.
 *
 * <p>Remember, the {@link TiffHeader#arbitraryNumber} and {@link TiffHeader#firstIfdOffset} fields are merely containers
 * for the bytes of those two fields in the header of the file.
 *
 * <p>The JDK interprets these bytes as big-endian, signed values, so printing the numbers with the JDK's default printing
 * will likely show the wrong value as it will interpret the bytes incorrectly.
 *
 * <p>The bytes of these fields should be interpreted as unsigned values following the declared byte order.
 */
public record TiffHeader(ByteOrder order, short arbitraryNumber, int firstIfdOffset) {

    public long unsignedFirstIfdOffset() {
        return Integer.toUnsignedLong(firstIfdOffset);
    }
}
