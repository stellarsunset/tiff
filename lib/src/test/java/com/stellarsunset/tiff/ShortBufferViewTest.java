package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ShortBufferViewTest {

    @Test
    void testReadShorts() {

        var buffer = BufferView.shorts(
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

        short[] a1 = buffer.readShorts(0, 1);
        assertArrayEquals(new short[]{0x010F}, a1);

        short[] a2 = buffer.readShorts(1, 1);
        assertArrayEquals(new short[]{(short) 0xF0FF}, a2);
    }
}
