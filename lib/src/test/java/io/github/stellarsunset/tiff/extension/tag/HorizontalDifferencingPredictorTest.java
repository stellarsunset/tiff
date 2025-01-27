package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.BufferView;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

        packAll(DifferencingPredictor.horizontal(1), bytes);
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

        unpackAll(DifferencingPredictor.horizontal(1), bytes);
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

        packAll(DifferencingPredictor.horizontal(3), bytes);
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

        unpackAll(DifferencingPredictor.horizontal(3), bytes);
        assertArrayEquals(expected, bytes);
    }

    private void packAll(DifferencingPredictor predictor, byte[][] bytes) {
        for (byte[] row : bytes) {
            predictor.pack(BufferView.bytes(ByteBuffer.wrap(row)));
        }
    }

    private void unpackAll(DifferencingPredictor predictor, byte[][] bytes) {
        for (byte[] row : bytes) {
            predictor.unpack(BufferView.bytes(ByteBuffer.wrap(row)));
        }
    }

    @Test
    void testOffsetArray() {
        var bytes = ByteBuffer.wrap(
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                2,
                4
        );

        var predictor = DifferencingPredictor.horizontal(1);

        predictor.pack(BufferView.bytes(bytes));
        assertArrayEquals(new byte[]{1, 2, 3, 1, 1, 1, 7, 8, 9, 10}, bytes.array());

        predictor.unpack(BufferView.bytes(bytes));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, bytes.array());
    }

    @Test
    void testCharArrayIdempotent() {
        char[] chars = new char[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        char[] expected = Arrays.copyOf(chars, chars.length);

        var predictor = DifferencingPredictor.horizontal(1);

        BufferView.Char view = BufferView.chars(
                StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars))
        );

        predictor.pack(view);
        predictor.unpack(view);

        assertArrayEquals(expected, chars);
    }

    @Test
    void testShortArrayIdempotent() {
        byte[] shorts = toByteArray(new short[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        byte[] expected = Arrays.copyOf(shorts, shorts.length);

        var predictor = DifferencingPredictor.horizontal(1);

        BufferView.Short view = BufferView.shorts(
                ByteBuffer.wrap(shorts)
        );

        predictor.pack(view);
        predictor.unpack(view);

        assertArrayEquals(expected, shorts);
    }

    private byte[] toByteArray(short[] shorts) {
        ByteBuffer bb = ByteBuffer.allocate(shorts.length * Short.BYTES);
        for (short s : shorts) {
            bb.putShort(s);
        }
        return bb.array();
    }

    @Test
    void testIntArrayIdempotent() {
        byte[] ints = toByteArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        byte[] expected = Arrays.copyOf(ints, ints.length);

        var predictor = DifferencingPredictor.horizontal(1);

        BufferView.Int view = BufferView.ints(
                ByteBuffer.wrap(ints)
        );

        predictor.pack(view);
        predictor.unpack(view);

        assertArrayEquals(expected, ints);
    }

    private byte[] toByteArray(int[] ints) {
        ByteBuffer bb = ByteBuffer.allocate(ints.length * Integer.BYTES);
        for (int i : ints) {
            bb.putInt(i);
        }
        return bb.array();
    }

    @Test
    void testLongArrayIdempotent() {
        byte[] longs = toByteArray(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        byte[] expected = Arrays.copyOf(longs, longs.length);

        var predictor = DifferencingPredictor.horizontal(1);

        BufferView.Long view = BufferView.longs(
                ByteBuffer.wrap(longs)
        );

        predictor.pack(view);
        predictor.unpack(view);

        assertArrayEquals(expected, longs);
    }

    private byte[] toByteArray(long[] longs) {
        ByteBuffer bb = ByteBuffer.allocate(longs.length * Long.BYTES);
        for (long l : longs) {
            bb.putLong(l);
        }
        return bb.array();
    }
}
