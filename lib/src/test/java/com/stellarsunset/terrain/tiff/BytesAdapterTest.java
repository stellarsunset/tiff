package com.stellarsunset.terrain.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BytesAdapterTest {

    @Test
    void testBigEndian() {

        BytesAdapter adapter = BytesAdapter.of(ByteOrder.BIG_ENDIAN);

        assertAll(
                () -> assertEquals((byte) 0x1, adapter.adaptRawByte((byte) 0x1), "Byte 0x1"),
                () -> assertEquals((short) 0x0001, adapter.adaptRawShort((short) 0x1), "Byte 0x01"),
                () -> assertEquals(0x00000001, adapter.adaptRawInt(0x1), "Byte 0x0001"),
                () -> assertEquals(0x1L, adapter.adaptRawLong(0x1), "Long 0x1")
        );
    }

    @Test
    void testLittleEndian() {

        BytesAdapter adapter = BytesAdapter.of(ByteOrder.LITTLE_ENDIAN);

        assertAll(
                () -> assertEquals((byte) 0x1, adapter.adaptRawByte((byte) 0x1), "Byte 0x1"),
                () -> assertEquals((short) 0x0100, adapter.adaptRawShort((short) 0x1), "Short 0x1"),
                () -> assertEquals(0x01000000, adapter.adaptRawInt(0x1), "Int 0x1"),
                () -> assertEquals(0x0100000000000000L, adapter.adaptRawLong(0x1), "Long 0x1")
        );
    }
}