package com.stellarsunset.tiff.extension.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DifferencingPredictorTest {

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

        DifferencingPredictor.planarOneByteComponents(1)
                .packAll(bytes);

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

        DifferencingPredictor.planarOneByteComponents(1)
                .unpackAll(bytes);

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

        DifferencingPredictor.planarOneByteComponents(3)
                .packAll(bytes);

        assertArrayEquals(expected, bytes);
    }

    @Test
    void testUnpackThreeComponents() {

    }
}
