package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LongBufferViewTest {

    @Test
    void testReadLongs() {

        var buffer = BufferView.longs(
                ByteBuffer.wrap(
                        new byte[]{
                                0x01, 0x0F, (byte) 0xF0, (byte) 0xFF, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFC,
                                0x01, 0x0F, (byte) 0xF0, (byte) 0xFF, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFF,
                                0x01, 0x0F, (byte) 0xF0, (byte) 0xFF, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFC
                        },
                        8,
                        16
                )
        );
        assertEquals(2, buffer.len());

        long[] a1 = buffer.readLongs(0, 1);
        assertArrayEquals(new long[]{0x010FF0FF010FF0FFL}, a1);

        long[] a2 = buffer.readLongs(1, 1);
        assertArrayEquals(new long[]{0x010FF0FF010FF0FCL}, a2);
    }
}
