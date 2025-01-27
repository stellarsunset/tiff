package io.github.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FloatBufferViewTest {

    @Test
    void testReadFloat() {

        var buffer = BufferView.floats(
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

        float[] a1 = buffer.readFloats(0, 1);
        assertArrayEquals(new float[]{Float.intBitsToFloat(0x010FF0FF)}, a1);

        float[] a2 = buffer.readFloats(1, 1);
        assertArrayEquals(new float[]{Float.intBitsToFloat(0x010FF0FC)}, a2);
    }
}
