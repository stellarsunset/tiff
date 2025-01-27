package io.github.stellarsunset.tiff.compress;

import io.github.stellarsunset.tiff.BytesAdapter;
import mil.nga.tiff.compression.RawCompression;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class UncompressedTest {

    private static final RawCompression REFERENCE = new RawCompression();

    private static final Uncompressed DECODER = new Uncompressed();

    @Test
    void test() {
        byte[] bytes = new byte[]{4, 4, 4, 4, 0, 1, 2, 3};
        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.BIG_ENDIAN));
        assertArrayEquals(bytes, actual);
    }

    @Test
    void regressionTest() {

        byte[] bytes = new byte[]{-128, -3, 4, 3, 0, 1, 2, 3};

        byte[] expected = REFERENCE.decode(bytes, ByteOrder.BIG_ENDIAN);
        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.BIG_ENDIAN));

        assertArrayEquals(expected, actual);
    }
}
