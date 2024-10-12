package com.stellarsunset.tiff;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Reader for an {@link Ifd} in a TIFF file and its constituent entries. This class is not lazy and materializes the
 * tag values.
 *
 * <p>Each 12-byte IFD entry has the following format:
 * <ol>
 *     <li>Bytes 0-1: The Tag that identifies the field</li>
 *     <li>Bytes 2-3: The field Type</li>
 *     <li>Bytes 4-7: The number of values, Count of the indicated Type</li>
 *     <li>Bytes 8-11: The Value Offset, the file offset (in bytes) of the Value for the field. The Value is expected
 *     to begin on a word boundary; the corresponding Value Offset will thus be an even number. This file offset may
 *     point anywhere in the file, even after the image data.</li>
 * </ol>
 *
 * <p>While the above is generally true, to save time and space the Value Offset contains the Value instead of pointing
 * to the Value if and only if the Value fits into 4 bytes.
 *
 * <p>If the Value is shorter than 4 bytes, it is left-justified within the 4-byte Value Offset, i.e., stored in the
 * lower numbered bytes. Whether the Value fits within 4 bytes is determined by the Type and Count of the field.
 *
 * <p>Note: entries in a TIFF IFD are sorted by their tag identifier.
 */
record IfdReader(BytesAdapter adapter) {

    /**
     * Read the {@link Ifd} in the TIFF file pointed to by the {@code channel} starting at the provided {@code offset}.
     *
     * @param channel  byte channel pointing to the underlying TIFF file
     * @param position the position in the file of the IFD
     */
    Ifd read(SeekableByteChannel channel, long position) throws IOException {

        BytesReader reader = new BytesReader(channel);

        short entryCount = adapter.adaptRawShort(
                reader.readBytes(position, 2).getShort(0)
        );

        int unsignedEntryCount = Short.toUnsignedInt(entryCount);
        int entriesBytes = unsignedEntryCount * 12;

        // Buffer of all the raw bytes of the IFD entries
        ByteBuffer entriesBuffer = reader.readBytes(position + 2, entriesBytes);

        Ifd.Entry[] entries = new Ifd.Entry[unsignedEntryCount];
        for (int i = 0; i < unsignedEntryCount; i++) {
            int byteOffset = i * 12;

            short tag = adapter.adaptRawShort(entriesBuffer.getShort(byteOffset));
            short type = adapter.adaptRawShort(entriesBuffer.getShort(byteOffset + 2));

            int count = adapter.adaptRawInt(entriesBuffer.getInt(byteOffset + 4));

            // we want the raw bytes of the value offset for handoff, this may be a pointer to a
            // position in the file OR a left-justified set of values totaling < 4 bytes
            int valueOffset = entriesBuffer.getInt(byteOffset + 8);
            entries[i] = entryMaker(reader, type).makeEntry(tag, count, valueOffset);
        }

        long nextOffsetPosition = position + 2 + entriesBytes;

        int nextIfdOffset = adapter.adaptRawInt(
                reader.readBytes(nextOffsetPosition, 4).getInt(0)
        );

        return new Ifd(entryCount, entries, nextIfdOffset);
    }

    private IfdEntryMaker entryMaker(BytesReader reader, short type) {
        int typeInt = Short.toUnsignedInt(type);
        var adapter = new ArrayBytesAdapter(adapter());
        return switch (typeInt) {
            case 1 -> new IfdEntryMaker.Byte(reader, adapter);
            case 2 -> new IfdEntryMaker.Ascii(reader, adapter);
            case 3 -> new IfdEntryMaker.Short(reader, adapter);
            case 4 -> new IfdEntryMaker.Long(reader, adapter);
            case 5 -> new IfdEntryMaker.Rational(reader, adapter);
            case 6 -> new IfdEntryMaker.SByte(reader, adapter);
            case 7 -> new IfdEntryMaker.Undefined(reader, adapter);
            case 8 -> new IfdEntryMaker.SShort(reader, adapter);
            case 9 -> new IfdEntryMaker.SLong(reader, adapter);
            case 10 -> new IfdEntryMaker.SRational(reader, adapter);
            case 11 -> new IfdEntryMaker.Float(reader, adapter);
            case 12 -> new IfdEntryMaker.Double(reader, adapter);
            default -> throw new IllegalArgumentException("Unknown IFD entry type: " + typeInt);
        };
    }
}
