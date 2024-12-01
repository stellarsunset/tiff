package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testReadAsBytes_BE() {

        BytesReader reader = new BytesReader(
                ByteArrayChannel.fromByteArray(new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF})
        );

        BytesAdapter adapter = BytesAdapter.of(ByteOrder.BIG_ENDIAN);

        int offset = reader.readBytes(0, 4)
                .getInt(0);

        byte[] countOne = adapter.readAsBytes(offset, 1);
        byte[] countTwo = adapter.readAsBytes(offset, 2);

        assertAll(
                () -> assertArrayEquals(new byte[]{0x01}, countOne, "One"),
                () -> assertArrayEquals(new byte[]{0x01, 0x0F}, countTwo, "Two")
        );
    }

    @Test
    void testReadAsBytes_LE() {

        BytesReader reader = new BytesReader(
                ByteArrayChannel.fromByteArray(new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF})
        );

        BytesAdapter adapter = BytesAdapter.of(ByteOrder.LITTLE_ENDIAN);

        int offset = reader.readBytes(0, 4)
                .getInt(0);

        byte[] countOne = adapter.readAsBytes(offset, 1);
        byte[] countTwo = adapter.readAsBytes(offset, 2);

        assertAll(
                () -> assertArrayEquals(new byte[]{(byte) 0x01}, countOne, "One"),
                () -> assertArrayEquals(new byte[]{(byte) 0x01, (byte) 0x0F}, countTwo, "Two")
        );
    }

    @Test
    void testReadAsShorts_BE() {

        BytesReader reader = new BytesReader(
                ByteArrayChannel.fromByteArray(new byte[]{0x00, 0x01, (byte) 0xFF, (byte) 0x00})
        );

        BytesAdapter adapter = BytesAdapter.of(ByteOrder.BIG_ENDIAN);

        int offset = reader.readBytes(0, 4)
                .getInt(0);

        short[] countOne = adapter.readAsShorts(offset, 1);
        short[] countTwo = adapter.readAsShorts(offset, 2);

        assertAll(
                () -> assertArrayEquals(new short[]{0x0001}, countOne, "One"),
                () -> assertArrayEquals(new short[]{0x0001, (short) 0xFF00}, countTwo, "Two")
        );
    }

    @Test
    void testReadAsShorts_LE() {

        BytesReader reader = new BytesReader(
                ByteArrayChannel.fromByteArray(new byte[]{0x00, 0x01, (byte) 0xFF, (byte) 0x00})
        );

        BytesAdapter adapter = BytesAdapter.of(ByteOrder.LITTLE_ENDIAN);

        int offset = reader.readBytes(0, 4)
                .getInt(0);

        short[] countOne = adapter.readAsShorts(offset, 1);
        short[] countTwo = adapter.readAsShorts(offset, 2);

        assertAll(
                () -> assertArrayEquals(new short[]{(short) 0x0100}, countOne, "One"),
                () -> assertArrayEquals(new short[]{(short) 0x0100, (short) 0x00FF}, countTwo, "Two")
        );
    }
}