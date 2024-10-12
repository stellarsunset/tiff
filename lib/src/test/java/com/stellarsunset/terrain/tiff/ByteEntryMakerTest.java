package com.stellarsunset.terrain.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ByteEntryMakerTest {

    @Test
    void testReader_ValueAtOffset_BE() {

        byte[] theArray = new byte[]{0x00, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFF};

        SeekableByteChannel channel = ByteArrayChannel.fromByteArray(theArray);

        IfdEntryMaker.Byte maker = new IfdEntryMaker.Byte(
                new BytesReader(channel),
                ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN)
        );

        byte[] values = maker.makeEntry((short) 0, 5, 0)
                .values();

        assertArrayEquals(theArray, values);
    }

    @Test
    void testReader_ValueAtOffset_LE() {

        byte[] theArray = new byte[]{0x00, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFF};

        SeekableByteChannel channel = ByteArrayChannel.fromByteArray(theArray);

        IfdEntryMaker.Byte maker = new IfdEntryMaker.Byte(
                new BytesReader(channel),
                ArrayBytesAdapter.of(ByteOrder.LITTLE_ENDIAN)
        );

        byte[] values = maker.makeEntry((short) 0, 5, 0)
                .values();

        assertArrayEquals(theArray, values);
    }
}
