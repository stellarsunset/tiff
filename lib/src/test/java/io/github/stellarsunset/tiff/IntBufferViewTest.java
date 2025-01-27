package io.github.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IntBufferViewTest {

    @Test
    void testReadInts() {

        var buffer = BufferView.ints(
                ByteBuffer.wrap(
                        new byte[]{
                                0x01, 0x0F, (byte) 0xF0, (byte) 0xFC,
                                0x01, 0x0F, (byte) 0xF0, (byte) 0xFF,
                                0x01, 0x0F, (byte) 0xF0, (byte) 0xFC
                        },
                        4,
                        8
                )
        );
        assertEquals(2, buffer.len());

        int[] a1 = buffer.readInts(0, 1);
        assertArrayEquals(new int[]{0x010FF0FF}, a1);

        int[] a2 = buffer.readInts(1, 1);
        assertArrayEquals(new int[]{0x010FF0FC}, a2);
    }
}
