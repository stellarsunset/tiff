package com.stellarsunset.tiff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Adapter class for converting bytes from an incoming file wrapped as a {@link ByteBuffer} into the correct endianness
 * so the JDK interprets the values of those bytes correctly.
 *
 * <p>We could do this directly through the {@link ByteBuffer} API but there are some fields in the files where we don't
 * want to immediately convert them to the correct endianness and instead need to inspect the contents to determine how
 * they should be interpreted (e.g. as left justified short/byte values in an integer field).
 *
 * <p>This class keeps it simple but makes it easier to defer that decision on when to convert the values.
 */
public interface BytesAdapter {

    static BytesAdapter of(ByteOrder order) {
        return ByteOrder.LITTLE_ENDIAN.equals(order) ? new LittleEndian() : new BigEndian();
    }

    ByteOrder order();

    byte adaptRawByte(byte rawByte);

    short adaptRawShort(short rawShort);

    int adaptRawInt(int rawInt);

    long adaptRawLong(long rawLong);

    /**
     * Attempt to read the integer value of the offset associated with an IFD entry as an array of left-justified
     * bytes, taking into account the endianness.
     */
    default byte[] readAsBytes(int rawInt, int count) {
        byte[] values = new byte[count];
        for (int i = 0, bytes = rawInt; i < count; i++) {
            values[i] = adaptRawByte((byte) (bytes >> 24));
            bytes = bytes << 8;
        }
        return values;
    }

    /**
     * Attempt to read the integer value of the offset associated with an IFD entry as an array of left-justified
     * shorts, taking into account the endianness.
     */
    default short[] readAsShorts(int rawInt, int count) {
        short[] values = new short[count];
        for (int i = 0, bytes = rawInt; i < count; i++) {
            values[i] = adaptRawShort((short) (bytes >> 16));
            bytes = bytes << 16;
        }
        return values;
    }

    record BigEndian() implements BytesAdapter {

        @Override
        public ByteOrder order() {
            return ByteOrder.BIG_ENDIAN;
        }

        @Override
        public byte adaptRawByte(byte rawByte) {
            return rawByte;
        }

        @Override
        public short adaptRawShort(short rawShort) {
            return rawShort;
        }

        @Override
        public int adaptRawInt(int rawInt) {
            return rawInt;
        }

        @Override
        public long adaptRawLong(long rawLong) {
            return rawLong;
        }
    }

    record LittleEndian() implements BytesAdapter {

        @Override
        public ByteOrder order() {
            return ByteOrder.LITTLE_ENDIAN;
        }

        @Override
        public byte adaptRawByte(byte rawByte) {
            return rawByte;
        }

        @Override
        public short adaptRawShort(short rawShort) {
            return Short.reverseBytes(rawShort);
        }

        @Override
        public int adaptRawInt(int rawInt) {
            return Integer.reverseBytes(rawInt);
        }

        @Override
        public long adaptRawLong(long rawLong) {
            return Long.reverseBytes(rawLong);
        }
    }
}
