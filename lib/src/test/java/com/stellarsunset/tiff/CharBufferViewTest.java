package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CharBufferViewTest {

    @Test
    void testReadChars() {

        var buffer = BufferView.chars(
                ByteBuffer.wrap(
                        new byte[]{
                                (byte) 0xF0, (byte) 0xFF,
                                0x01, 0x0F,
                                (byte) 0xF0, (byte) 0xFF
                        },
                        2,
                        4
                )
        );
        assertEquals(2, buffer.len());

        char[] a1 = buffer.readChars(0, 1);
        assertArrayEquals(new char[]{0x010F}, a1);

        char[] a2 = buffer.readChars(1, 1);
        assertArrayEquals(new char[]{(char) 0xF0FF}, a2);
    }
}
