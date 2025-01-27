package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.BufferView;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Disabled("Not fully supported yet")
class FloatingPointDifferencingPredictorTest {

    @Test
    void testPack() {

        byte[] floats = toByteArray(
                new float[]{
                        Float.intBitsToFloat(0x01234567),
                        Float.intBitsToFloat(0x89ABCDEF)
                }
        );

        byte[] expected = new byte[]{
                0x01, (byte) 0x89, // exp hi
                0x23, (byte) 0xAB, // exp lo
                0x45, (byte) 0xCD, // mantissa hi
                0x67, (byte) 0xEF  // mantissa lo
        };

        BufferView.Byte view = BufferView.bytes(
                ByteBuffer.wrap(floats)
        );

        var predictor = DifferencingPredictor.floatingPoint(1);
        predictor.pack(view);

        assertArrayEquals(expected, floats);
    }

    @Test
    void testUnpack() {

        byte[] floats = new byte[]{
                0x01, (byte) 0x89, // exp hi
                0x23, (byte) 0xAB, // exp lo
                0x45, (byte) 0xCD, // mantissa hi
                0x67, (byte) 0xEF  // mantissa lo
        };

        byte[] expected = toByteArray(
                new float[]{
                        Float.intBitsToFloat(0x01234567),
                        Float.intBitsToFloat(0x89ABCDEF)
                }
        );

        BufferView.Byte view = BufferView.bytes(
                ByteBuffer.wrap(floats)
        );

        var predictor = DifferencingPredictor.floatingPoint(1);
        predictor.unpack(view);

        assertArrayEquals(expected, floats);
    }

    @Test
    void testFloatArrayIdempotent() {

        byte[] floats = toByteArray(new float[]{1.0f, 2.0f, 3.0f, 4.0f});
        byte[] expected = Arrays.copyOf(floats, floats.length);

        var predictor = DifferencingPredictor.floatingPoint(1);

        BufferView.Byte view = BufferView.bytes(
                ByteBuffer.wrap(floats)
        );

        predictor.pack(view);
        predictor.unpack(view);

        assertArrayEquals(expected, floats);
    }

    private byte[] toByteArray(float[] floats) {
        ByteBuffer bb = ByteBuffer.allocate(floats.length * Float.BYTES);
        for (float f : floats) {
            bb.putFloat(f);
        }
        return bb.array();
    }
}
