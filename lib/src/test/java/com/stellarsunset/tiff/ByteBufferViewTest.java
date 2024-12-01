package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteBufferViewTest {

    @Test
    void testReadBytes() {

        var buffer = BufferView.bytes(
                ByteBuffer.wrap(
                        new byte[]{(byte) 0xF0, (byte) 0xFF, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFF},
                        2,
                        4
                )
        );
        assertEquals(4, buffer.len());

        byte[] a1 = buffer.readBytes(0, 2);
        assertArrayEquals(new byte[]{0x01, 0x0F}, a1);

        byte[] a2 = buffer.readBytes(2, 2);
        assertArrayEquals(new byte[]{(byte) 0xF0, (byte) 0xFF}, a2);
    }
}
