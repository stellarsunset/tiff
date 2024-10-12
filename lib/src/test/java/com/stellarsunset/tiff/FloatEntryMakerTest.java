package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Special Integers:  {@code 0.1f = 3dcccccd}, {@code 1.0f = 3f800000}, {@code 10.0f = 41200000}
 */
class FloatEntryMakerTest {

    @Test
    void testReader_ValueAtOffset_BE() {

        SeekableByteChannel channel = ByteArrayChannel.fromIntArray(
                new int[]{0x3DCCCCCD, 0x3F800000, 0x41200000}
        );

        IfdEntryMaker.Float maker = new IfdEntryMaker.Float(
                new BytesReader(channel),
                ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN)
        );

        float[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new float[]{0.1f, 1.0f, 10.0f}, values);
    }

    @Test
    void testReader_ValueAtOffset_LE() {

        SeekableByteChannel channel = ByteArrayChannel.fromIntArray(
                new int[]{0xCDCCCC3D, 0x0000803F, 0x00002041}
        );

        IfdEntryMaker.Float maker = new IfdEntryMaker.Float(
                new BytesReader(channel),
                ArrayBytesAdapter.of(ByteOrder.LITTLE_ENDIAN)
        );

        float[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new float[]{0.1f, 1.0f, 10.0f}, values);
    }
}
