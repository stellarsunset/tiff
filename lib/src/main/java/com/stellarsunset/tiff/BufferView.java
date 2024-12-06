package com.stellarsunset.tiff;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Returns a handle for a {@link ByteBuffer} that is tied to a specific range of bytes.
 *
 * <p>The key difference between this and a normal {@link ByteBuffer} is the {@code getByte(int)} and friends calls use
 * indices <i>after</i> the startByte point instead of an absolute offset as in the standard {@link ByteBuffer#get(int)}.
 *
 * <p>The indexing methods on {@code getShort(int)}, {@code getInt(int)} also refer to the index of the next logical value
 * of the given type (e.g. short, int, long) within that range (as opposed to the byte offset).
 *
 * <p>Together these classes prevents errors in callers like the following:
 * <pre>{@code
 *     /// populate an array of shorts from the buffer
 *     ByteBuffer buffer = ByteBuffer.wrap(...);
 *     short[] shorts = new short[numberOfShorts];
 *     for (int i = 0; i < numberOfShorts; i++) {
 *         shorts[i] = buffer.getShort(i);
 *     }
 *     // wrong because, 'i' is the byte index, not the index of the next would-be short in the buffer
 *     // so i=0 and i=1 getShort() calls would return shorts overlapping by a byte
 *
 *     // access the 5th byte within the range specified to the ByteBuffer.wrap(...)
 *     ByteBuffer buffer = ByteBuffer.wrap(array, offset=10, length=20);
 *     byte b = buffer.get(5);
 *     // wrong, get requires an absolute index within the underlying array, so you'd have to use:
 *     b = buffer.get(offset + 5)
 * }</pre>
 */
public sealed interface BufferView {

    static Byte bytes(ByteBuffer buffer) {
        return new Byte(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    static Char chars(ByteBuffer buffer) {
        return new Char(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    static Short shorts(ByteBuffer buffer) {
        return new Short(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    static Int ints(ByteBuffer buffer) {
        return new Int(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    static Float floats(ByteBuffer buffer) {
        return new Float(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    static Long longs(ByteBuffer buffer) {
        return new Long(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    static Double doubles(ByteBuffer buffer) {
        return new Double(buffer, buffer.position(), buffer.limit() - buffer.position());
    }

    /**
     * The underlying {@link ByteBuffer} calls are being delegated to.
     */
    ByteBuffer delegate();

    /**
     * The number of entries in the {@link BufferView}.
     *
     * <p>Specifically this is the number of elements of the <i>type</i> of data the view presents.
     */
    int len();

    record Byte(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        @Override
        public int len() {
            return lengthBytes;
        }

        public byte getByte(int index) {
            return delegate.get(absolute(index));
        }

        public Byte putByte(int index, byte value) {
            delegate.put(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index;
        }

        public byte[] readBytes(int index, int count) {
            int max = index + count;
            byte[] bytes = new byte[count];
            for (int i = index; i < max; i++) {
                bytes[i - index] = getByte(i);
            }
            return bytes;
        }
    }

    record Char(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        public Char {
            checkArgument(lengthBytes % java.lang.Character.BYTES == 0);
        }

        @Override
        public int len() {
            return lengthBytes / Character.BYTES;
        }

        public char getChar(int index) {
            return delegate.getChar(absolute(index));
        }

        public Char putChar(int index, char value) {
            delegate.putChar(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index * Character.BYTES;
        }

        public char[] readChars(int index, int count) {
            int max = index + count;
            char[] chars = new char[count];
            for (int i = index; i < max; i++) {
                chars[i - index] = getChar(i);
            }
            return chars;
        }
    }

    record Short(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        public Short {
            checkArgument(lengthBytes % java.lang.Short.BYTES == 0);
        }

        @Override
        public int len() {
            return lengthBytes / java.lang.Short.BYTES;
        }

        public short getShort(int index) {
            return delegate.getShort(absolute(index));
        }

        public Short putShort(int index, short value) {
            delegate.putShort(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index * java.lang.Short.BYTES;
        }

        public short[] readShorts(int index, int count) {
            int max = index + count;
            short[] shorts = new short[count];
            for (int i = index; i < max; i++) {
                shorts[i - index] = getShort(i);
            }
            return shorts;
        }
    }

    record Int(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        public Int {
            checkArgument(lengthBytes % Integer.BYTES == 0);
        }

        @Override
        public int len() {
            return lengthBytes / Integer.BYTES;
        }

        public int getInt(int index) {
            return delegate.getInt(absolute(index));
        }

        public Int putInt(int index, int value) {
            delegate.putInt(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index * Integer.BYTES;
        }

        public int[] readInts(int index, int count) {
            int max = index + count;
            int[] ints = new int[count];
            for (int i = index; i < max; i++) {
                ints[i - index] = getInt(i);
            }
            return ints;
        }
    }

    record Float(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        public Float {
            checkArgument(lengthBytes % java.lang.Float.BYTES == 0);
        }

        @Override
        public int len() {
            return lengthBytes / java.lang.Float.BYTES;
        }

        public float getFloat(int index) {
            return delegate.getFloat(absolute(index));
        }

        public Float putFloat(int index, float value) {
            delegate.putFloat(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index * java.lang.Float.BYTES;
        }

        public float[] readFloats(int index, int count) {
            int max = index + count;
            float[] floats = new float[count];
            for (int i = index; i < max; i++) {
                floats[i - index] = getFloat(i);
            }
            return floats;
        }
    }

    record Long(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        public Long {
            checkArgument(lengthBytes % java.lang.Long.BYTES == 0);
        }

        @Override
        public int len() {
            return lengthBytes / java.lang.Long.BYTES;
        }

        public long getLong(int index) {
            return delegate.getLong(absolute(index));
        }

        public Long putLong(int index, long value) {
            delegate.putLong(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index * java.lang.Long.BYTES;
        }

        public long[] readLongs(int index, int count) {
            int max = index + count;
            long[] longs = new long[count];
            for (int i = index; i < max; i++) {
                longs[i - index] = getLong(i);
            }
            return longs;
        }
    }

    record Double(ByteBuffer delegate, int startByte, int lengthBytes) implements BufferView {

        public Double {
            checkArgument(lengthBytes % java.lang.Double.BYTES == 0);
        }

        @Override
        public int len() {
            return lengthBytes / java.lang.Double.BYTES;
        }

        public double getDouble(int index) {
            return delegate.getDouble(absolute(index));
        }

        public Double putDouble(int index, double value) {
            delegate.putDouble(absolute(index), value);
            return this;
        }

        private int absolute(int index) {
            return startByte + index * java.lang.Double.BYTES;
        }

        public double[] readDoubles(int index, int count) {
            int max = index + count;
            double[] doubles = new double[count];
            for (int i = index; i < max; i++) {
                doubles[i - index] = getDouble(i);
            }
            return doubles;
        }
    }
}
