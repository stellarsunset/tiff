package io.github.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Special Longs:  {@code 0.1d = 3fb999999999999a}, {@code 1.0d = 3ff0000000000000}, {@code 10.0d = 4024000000000000}
 */
class DoubleEntryMakerTest {

    @Test
    void testReader_ValueAtOffset_BE() {

        SeekableByteChannel channel = ByteArrayChannel.fromLongArray(
                new long[]{0x3fb999999999999aL, 0x3ff0000000000000L, 0x4024000000000000L}
        );

        IfdEntryMaker.Double maker = new IfdEntryMaker.Double(
                new BytesReader(channel),
                BytesAdapter.of(ByteOrder.BIG_ENDIAN)
        );

        double[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new double[]{0.1d, 1.0d, 10.0d}, values);
    }

    @Test
    void testReader_ValueAtOffset_LE() {

        SeekableByteChannel channel = ByteArrayChannel.fromLongArray(
                new long[]{0x9a9999999999b93fL, 0x000000000000f03fL, 0x0000000000002440L}
        );

        IfdEntryMaker.Double maker = new IfdEntryMaker.Double(
                new BytesReader(channel),
                BytesAdapter.of(ByteOrder.LITTLE_ENDIAN)
        );

        double[] values = maker.makeEntry((short) 0, 3, 0)
                .values();

        assertArrayEquals(new double[]{0.1d, 1.0d, 10.0d}, values);
    }
}
