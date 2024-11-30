package com.stellarsunset.tiff.extension.tag;

import org.junit.jupiter.api.Test;

import java.nio.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HorizontalDifferencingPredictorTest {

    @Test
    void testPackOneComponent() {

        byte[][] bytes = new byte[][]{
                new byte[]{1, 2, 3, 4, 5, 6},
                new byte[]{-1, -2, -3, -4, -5, -6},
                new byte[]{10, 20, 30, 30, 20, 10}
        };

        byte[][] expected = new byte[][]{
                new byte[]{1, 1, 1, 1, 1, 1},
                new byte[]{-1, -1, -1, -1, -1, -1},
                new byte[]{10, 10, 10, 0, -10, -10}
        };

        DifferencingPredictor.horizontal(1)
                .packAll(ByteOrder.BIG_ENDIAN, bytes);

        assertArrayEquals(expected, bytes);
    }

    @Test
    void testUnpackOneComponent() {

        byte[][] bytes = new byte[][]{
                new byte[]{1, 1, 1, 1, 1, 1},
                new byte[]{-1, -1, -1, -1, -1, -1},
                new byte[]{10, 10, 10, 0, -10, -10}
        };

        byte[][] expected = new byte[][]{
                new byte[]{1, 2, 3, 4, 5, 6},
                new byte[]{-1, -2, -3, -4, -5, -6},
                new byte[]{10, 20, 30, 30, 20, 10}
        };

        DifferencingPredictor.horizontal(1)
                .unpackAll(ByteOrder.BIG_ENDIAN, bytes);

        assertArrayEquals(expected, bytes);
    }

    @Test
    void testPackThreeComponents() {

        byte[][] bytes = new byte[][]{
                new byte[]{1, 2, 1, 5, 4, 5, 7, 8, 7},
                new byte[]{-1, -2, -3, -4, -5, -6, -7, -8, -9},
                new byte[]{10, 20, 30, 40, 40, 30, 20, 10, 0}
        };

        byte[][] expected = new byte[][]{
                new byte[]{1, 2, 1, 4, 2, 4, 2, 4, 2},
                new byte[]{-1, -2, -3, -3, -3, -3, -3, -3, -3},
                new byte[]{10, 20, 30, 30, 20, 0, -20, -30, -30}
        };

        DifferencingPredictor.horizontal(3)
                .packAll(ByteOrder.BIG_ENDIAN, bytes);

        assertArrayEquals(expected, bytes);
    }

    @Test
    void testUnpackThreeComponents() {

        byte[][] bytes = new byte[][]{
                new byte[]{1, 2, 1, 4, 2, 4, 2, 4, 2},
                new byte[]{-1, -2, -3, -3, -3, -3, -3, -3, -3},
                new byte[]{10, 20, 30, 30, 20, 0, -20, -30, -30}
        };

        byte[][] expected = new byte[][]{
                new byte[]{1, 2, 1, 5, 4, 5, 7, 8, 7},
                new byte[]{-1, -2, -3, -4, -5, -6, -7, -8, -9},
                new byte[]{10, 20, 30, 40, 40, 30, 20, 10, 0}
        };

        DifferencingPredictor.horizontal(3)
                .unpackAll(ByteOrder.BIG_ENDIAN, bytes);

        assertArrayEquals(expected, bytes);
    }

    @Test
    void testOffsetArray() {
        byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        var predictor = DifferencingPredictor.horizontal(1);

        predictor.pack(ByteBuffer.wrap(bytes, 2, 4));
        assertArrayEquals(new byte[]{1, 2, 3, 1, 1, 1, 7, 8, 9, 10}, bytes);

        predictor.unpack(ByteBuffer.wrap(bytes, 2, 4));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, bytes);
    }

    @Test
    void testCharArrayIdempotent() {
        char[] chars = new char[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        char[] expected = new char[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        var predictor = DifferencingPredictor.horizontal(1);

        predictor.pack(CharBuffer.wrap(chars));
        predictor.unpack(CharBuffer.wrap(chars));

        assertArrayEquals(expected, chars);
    }

    @Test
    void testShortArrayIdempotent() {
        short[] shorts = new short[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        short[] expected = new short[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        var predictor = DifferencingPredictor.horizontal(1);

        predictor.pack(ShortBuffer.wrap(shorts));
        predictor.unpack(ShortBuffer.wrap(shorts));

        assertArrayEquals(expected, shorts);
    }

    @Test
    void testIntArrayIdempotent() {
        int[] ints = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] expected = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        var predictor = DifferencingPredictor.horizontal(1);

        predictor.pack(IntBuffer.wrap(ints));
        predictor.unpack(IntBuffer.wrap(ints));

        assertArrayEquals(expected, ints);
    }

    @Test
    void testLongArrayIdempotent() {
        long[] longs = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        long[] expected = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        var predictor = DifferencingPredictor.horizontal(1);

        predictor.pack(LongBuffer.wrap(longs));
        predictor.unpack(LongBuffer.wrap(longs));

        assertArrayEquals(expected, longs);
    }
}
