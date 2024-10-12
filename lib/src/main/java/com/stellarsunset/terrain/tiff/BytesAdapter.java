package com.stellarsunset.terrain.tiff;

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

    byte adaptRawByte(byte rawByte);

    short adaptRawShort(short rawShort);

    int adaptRawInt(int rawInt);

    long adaptRawLong(long rawLong);

    record BigEndian() implements BytesAdapter {

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
