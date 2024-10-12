package com.stellarsunset.terrain.tiff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of a {@link BytesAdapter} that also provides convenience methods for working with arrays as required
 * in a number of places in this class.
 */
record ArrayBytesAdapter(BytesAdapter adapter) implements BytesAdapter {

    static ArrayBytesAdapter of(ByteOrder order) {
        return new ArrayBytesAdapter(BytesAdapter.of(order));
    }

    @Override
    public byte adaptRawByte(byte rawByte) {
        return adapter.adaptRawByte(rawByte);
    }

    @Override
    public short adaptRawShort(short rawShort) {
        return adapter.adaptRawShort(rawShort);
    }

    @Override
    public int adaptRawInt(int rawInt) {
        return adapter.adaptRawInt(rawInt);
    }

    @Override
    public long adaptRawLong(long rawLong) {
        return adapter.adaptRawLong(rawLong);
    }

    /**
     * Attempt to read the integer value of the offset associated with an IFD entry as an array of left-justified
     * bytes, taking into account the endianness.
     */
    public byte[] readAsBytes(int rawInt, int count) {
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
    public short[] readAsShorts(int rawInt, int count) {
        short[] values = new short[count];
        for (int i = 0, bytes = rawInt; i < count; i++) {
            values[i] = adaptRawShort((short) (bytes >> 16));
            bytes = bytes << 16;
        }
        return values;
    }

    /**
     * Read {@code count} bytes from the buffer starting at the provided {@code offset} in the buffer.
     *
     * <p>This method handles the endian-ness conversions be delegating to {@link #adaptRawByte(byte)}.
     *
     * @param buffer the buffer to read bytes from
     * @param offset the offset in the buffer to start the read from
     * @param count  the number of byte values to read from the buffer after the offset
     */
    public byte[] readBytes(ByteBuffer buffer, int offset, int count) {
        byte[] array = new byte[count];
        for (int i = offset; i < count; i++) {
            array[i - offset] = adaptRawByte(buffer.get(i));
        }
        return array;
    }

    /**
     * Read {@code count} shorts from the buffer starting at the provided {@code offset} in the buffer.
     *
     * <p>This method handles the endian-ness conversions be delegating to {@link #adaptRawShort(short)}.
     *
     * @param buffer the buffer to read shorts from
     * @param offset the offset in the buffer to start the read from
     * @param count  the number of short values to read from the buffer after the offset
     */
    public short[] readShorts(ByteBuffer buffer, int offset, int count) {
        short[] array = new short[count];
        for (int i = offset; i < count; i++) {
            array[i - offset] = adaptRawShort(buffer.getShort(i * 2));
        }
        return array;
    }

    /**
     * Read {@code count} ints from the buffer starting at the provided {@code offset} in the buffer.
     *
     * <p>This method handles the endian-ness conversions be delegating to {@link #adaptRawInt(int)}.
     *
     * @param buffer the buffer to read ints from
     * @param offset the offset in the buffer to start the read from
     * @param count  the number of int values to read from the buffer after the offset
     */
    public int[] readInts(ByteBuffer buffer, int offset, int count) {
        int[] array = new int[count];
        for (int i = offset; i < count; i++) {
            array[i - offset] = adaptRawInt(buffer.getInt(i * 4));
        }
        return array;
    }

    /**
     * Read {@code count} longs from the buffer starting at the provided {@code offset} in the buffer.
     *
     * <p>This method handles the endian-ness conversions be delegating to {@link #adaptRawLong(long)}.
     *
     * @param buffer the buffer to read longs from
     * @param offset the offset in the buffer to start the read from
     * @param count  the number of long values to read from the buffer after the offset
     */
    public long[] readLongs(ByteBuffer buffer, int offset, int count) {
        long[] array = new long[count];
        for (int i = offset; i < count; i++) {
            array[i - offset] = adaptRawLong(buffer.getLong(i * 8));
        }
        return array;
    }
}
