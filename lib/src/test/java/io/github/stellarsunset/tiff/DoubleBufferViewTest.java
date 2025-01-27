package io.github.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleBufferViewTest {

    @Test
    void testReadDoubles() {

        var buffer = BufferView.doubles(
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

        double[] a1 = buffer.readDoubles(0, 1);
        assertArrayEquals(new double[]{Double.longBitsToDouble(0x010FF0FF010FF0FFL)}, a1);

        double[] a2 = buffer.readDoubles(1, 1);
        assertArrayEquals(new double[]{Double.longBitsToDouble(0x010FF0FF010FF0FCL)}, a2);
    }
}
