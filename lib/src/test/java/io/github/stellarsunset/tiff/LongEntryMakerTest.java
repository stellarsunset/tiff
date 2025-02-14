package io.github.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LongEntryMakerTest {

    @Test
    void testReader_ValueAtOffset_BE() {

        SeekableByteChannel channel = ByteArrayChannel.fromIntArray(
                new int[]{0x0F, 0xF0, 0x12}
        );

        IfdEntryMaker.Long maker = new IfdEntryMaker.Long(
                new BytesReader(channel),
                BytesAdapter.of(ByteOrder.BIG_ENDIAN)
        );

        int[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new int[]{0x0F, 0xF0, 0x12}, values);
    }

    @Test
    void testReader_ValueAtOffset_LE() {

        SeekableByteChannel channel = ByteArrayChannel.fromIntArray(
                new int[]{0x0F, 0xF0, 0x12}
        );

        IfdEntryMaker.Long maker = new IfdEntryMaker.Long(
                new BytesReader(channel),
                BytesAdapter.of(ByteOrder.LITTLE_ENDIAN)
        );

        int[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new int[]{0x0F000000, 0xF0000000, 0x12000000}, values);
    }
}
