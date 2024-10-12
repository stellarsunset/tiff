package com.stellarsunset.tiff;

import java.nio.ByteBuffer;

sealed interface IfdEntryMaker {

    /**
     * Create a new {@link Ifd.Entry} with the provided {@code tag} and with a {@code count} of values located at the
     * provided {@code rawValueOffsetBytes} in the file.
     *
     * @param tag                 the tag to use for the IFD entry
     * @param count               the number of values associated with the tag
     * @param rawValueOffsetBytes either (1) the offset in the file of the value OR (2) the left-justified values if they
     *                            total less than 4 bytes
     */
    Ifd.Entry makeEntry(short tag, int count, int rawValueOffsetBytes);

    record Byte(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Byte makeEntry(short tag, int count, int rawValueOffsetBytes) {

            byte[] values = count > 4
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : adapter.readAsBytes(rawValueOffsetBytes, count);

            return new Ifd.Entry.Byte(tag, values);
        }

        private byte[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count
            );
            return adapter.readBytes(
                    buffer,
                    0,
                    count
            );
        }
    }

    record Ascii(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Ascii makeEntry(short tag, int count, int rawValueOffsetBytes) {

            byte[] values = count > 4
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : adapter.readAsBytes(rawValueOffsetBytes, count);

            return new Ifd.Entry.Ascii(tag, values);
        }

        private byte[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count
            );
            return adapter.readBytes(
                    buffer,
                    0,
                    count
            );
        }
    }

    record Short(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Short makeEntry(short tag, int count, int rawValueOffsetBytes) {

            short[] values = count > 2
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : adapter.readAsShorts(rawValueOffsetBytes, count);

            return new Ifd.Entry.Short(tag, values);
        }

        private short[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 2
            );
            return adapter.readShorts(
                    buffer,
                    0,
                    count
            );
        }
    }

    record Long(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Long makeEntry(short tag, int count, int rawValueOffsetBytes) {

            int[] values = count > 1
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : new int[]{adapter.adaptRawInt(rawValueOffsetBytes)};

            return new Ifd.Entry.Long(tag, values);
        }

        private int[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 4
            );
            return adapter.readInts(
                    buffer,
                    0,
                    count
            );
        }
    }

    record Rational(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Rational makeEntry(short tag, int count, int rawValueOffsetBytes) {

            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 8
            );

            int[][] rationals = split(
                    adapter.readInts(buffer, 0, count * 2)
            );

            return new Ifd.Entry.Rational(tag, rationals[0], rationals[1]);
        }

        private static int[][] split(int[] numeratorsAndDenominators) {
            int[][] split = new int[2][numeratorsAndDenominators.length / 2];
            for (int i = 0; i < numeratorsAndDenominators.length; i++) {
                split[i % 2][i / 2] = numeratorsAndDenominators[i];
            }
            return split;
        }
    }

    record SByte(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.SByte makeEntry(short tag, int count, int rawValueOffsetBytes) {

            byte[] values = count > 4
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : adapter.readAsBytes(rawValueOffsetBytes, count);

            return new Ifd.Entry.SByte(tag, values);
        }

        private byte[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count
            );
            return adapter.readBytes(
                    buffer,
                    0,
                    count
            );
        }
    }

    record Undefined(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Undefined makeEntry(short tag, int count, int rawValueOffsetBytes) {

            // This shouldn't modify the underlying bytes, we don't know how to interpret
            byte[] values = count > 4
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : adapter.readAsBytes(rawValueOffsetBytes, count);

            return new Ifd.Entry.Undefined(tag, values);
        }

        private byte[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count
            );
            return adapter.readBytes(
                    buffer,
                    0,
                    count
            );
        }
    }

    record SShort(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.SShort makeEntry(short tag, int count, int rawValueOffsetBytes) {

            short[] values = count > 2
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : adapter.readAsShorts(rawValueOffsetBytes, count);

            return new Ifd.Entry.SShort(tag, values);
        }

        private short[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 2
            );
            return adapter.readShorts(
                    buffer,
                    0,
                    count
            );
        }
    }

    record SLong(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.SLong makeEntry(short tag, int count, int rawValueOffsetBytes) {

            int[] values = count > 1
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : new int[]{adapter.adaptRawInt(rawValueOffsetBytes)};

            return new Ifd.Entry.SLong(tag, values);
        }

        private int[] readValueAtOffset(int rawValueOffsetBytes, int count) {
            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 4
            );
            return adapter.readInts(
                    buffer,
                    0,
                    count
            );
        }
    }

    record SRational(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.SRational makeEntry(short tag, int count, int rawValueOffsetBytes) {

            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 8
            );

            int[][] rationals = Rational.split(
                    adapter.readInts(buffer, 0, count * 2)
            );

            return new Ifd.Entry.SRational(tag, rationals[0], rationals[1]);
        }
    }

    record Float(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Float makeEntry(short tag, int count, int rawValueOffsetBytes) {

            float[] values = count > 1
                    ? readValueAtOffset(rawValueOffsetBytes, count)
                    : new float[]{java.lang.Float.intBitsToFloat(rawValueOffsetBytes)};

            return new Ifd.Entry.Float(tag, values);
        }

        private float[] readValueAtOffset(int rawValueOffsetBytes, int count) {

            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 4
            );

            float[] array = new float[count];
            for (int i = 0; i < count; i++) {
                array[i] = java.lang.Float.intBitsToFloat(
                        adapter.adaptRawInt(buffer.getInt(i * 4))
                );
            }

            return array;
        }
    }

    record Double(BytesReader reader, ArrayBytesAdapter adapter) implements IfdEntryMaker {
        @Override
        public Ifd.Entry.Double makeEntry(short tag, int count, int rawValueOffsetBytes) {

            ByteBuffer buffer = reader.readBytes(
                    Integer.toUnsignedLong(adapter.adaptRawInt(rawValueOffsetBytes)),
                    count * 8
            );

            double[] array = new double[count];
            for (int i = 0; i < count; i++) {
                array[i] = java.lang.Double.longBitsToDouble(
                        adapter.adaptRawLong(buffer.getLong(i * 8))
                );
            }

            return new Ifd.Entry.Double(tag, array);
        }
    }
}
