package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.BytesAdapter;
import mil.nga.tiff.compression.LZWCompression;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LzwTest {

    private static final LZWCompression REFERENCE = new LZWCompression();

    private static final Lzw DECODER = new Lzw();

    @Test
    @Disabled("Not implemented")
    void test() {
        byte[] bytes = new byte[]{-128, -3, 4, 3, 0, 1, 2, 3};

        byte[] expected = new byte[]{4, 4, 4, 4, 0, 1, 2, 3};
        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.BIG_ENDIAN));

        assertArrayEquals(expected, actual);
    }

    @Test
    @Disabled("Not implemented")
    void regressionTest() {

        byte[] bytes = new byte[]{-128, -3, 4, 3, 0, 1, 2, 3};

        byte[] expected = REFERENCE.decode(bytes, ByteOrder.BIG_ENDIAN);
        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.BIG_ENDIAN));

        assertArrayEquals(expected, actual);
    }
}