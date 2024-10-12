package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.BytesAdapter;
import mil.nga.tiff.compression.PackbitsCompression;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PackBitsTest {

    private static final PackbitsCompression REFERENCE = new PackbitsCompression();

    private static final PackBits DECODER = new PackBits();

    @Test
    void test() {
        byte[] bytes = new byte[]{-128, -3, 4, 3, 0, 1, 2, 3};

        byte[] expected = new byte[]{4, 4, 4, 4, 0, 1, 2, 3};
        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.BIG_ENDIAN));

        assertArrayEquals(expected, actual);
    }

    @Test
    void regressionTest() {

        byte[] bytes = new byte[]{-128, -3, 4, 3, 0, 1, 2, 3};

        byte[] expected = REFERENCE.decode(bytes, ByteOrder.BIG_ENDIAN);
        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.BIG_ENDIAN));

        assertArrayEquals(expected, actual);
    }
}
