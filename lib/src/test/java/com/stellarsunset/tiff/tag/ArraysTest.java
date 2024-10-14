package com.stellarsunset.tiff.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ArraysTest {

    @Test
    void testShortsToUnsignedIntArray() {
        int[] actual = Arrays.toUnsignedIntArray(new short[]{-1, 0, 1});
        assertArrayEquals(new int[]{65535, 0, 1}, actual);
    }

    @Test
    void testShortsToUnsignedLongArray() {
        long[] actual = Arrays.toUnsignedLongArray(new short[]{-1, 0, 1});
        assertArrayEquals(new long[]{65535, 0, 1}, actual);
    }

    @Test
    void testIntsToUnsignedLongArray() {
        long[] actual = Arrays.toUnsignedLongArray(new int[]{-1, 0, 1});
        assertArrayEquals(new long[]{4294967295L, 0, 1}, actual);
    }
}
