package com.stellarsunset.terrain.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SShortEntryMakerTest {

    @Test
    void testReader_ValueAtOffset_BE() {

        SeekableByteChannel channel = ByteArrayChannel.fromShortArray(
                new short[]{0x0F, 0xF0, 0x12}
        );

        IfdEntryMaker.SShort maker = new IfdEntryMaker.SShort(
                new BytesReader(channel),
                ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN)
        );

        short[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new short[]{0x0F, 0xF0, 0x12}, values);
    }

    @Test
    void testReader_ValueAtOffset_LE() {

        SeekableByteChannel channel = ByteArrayChannel.fromShortArray(
                new short[]{0x000F, 0x00F0, 0x0012}
        );

        IfdEntryMaker.SShort maker = new IfdEntryMaker.SShort(
                new BytesReader(channel),
                ArrayBytesAdapter.of(ByteOrder.LITTLE_ENDIAN)
        );

        short[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new short[]{0x0F00, (short) 0xF000, 0x1200}, values);
    }
}
