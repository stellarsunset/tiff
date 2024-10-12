package com.stellarsunset.terrain.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ArrayBytesAdapterTest {

    @Test
    void testReadAsBytes_BE() {

        BytesReader reader = new BytesReader(
                ByteArrayChannel.fromByteArray(new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF})
        );

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN);

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

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.LITTLE_ENDIAN);

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

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN);

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

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.LITTLE_ENDIAN);

        int offset = reader.readBytes(0, 4)
                .getInt(0);

        short[] countOne = adapter.readAsShorts(offset, 1);
        short[] countTwo = adapter.readAsShorts(offset, 2);

        assertAll(
                () -> assertArrayEquals(new short[]{(short) 0x0100}, countOne, "One"),
                () -> assertArrayEquals(new short[]{(short) 0x0100, (short) 0x00FF}, countTwo, "Two")
        );
    }

    @Test
    void testReadBytes() {

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN);

        ByteBuffer buffer = ByteBuffer.wrap(
                new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF}
        );

        byte[] array = adapter.readBytes(buffer, 0, 2);
        assertArrayEquals(new byte[]{0x01, 0x0F}, array);
    }

    @Test
    void testReadShorts() {

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN);

        ByteBuffer buffer = ByteBuffer.wrap(
                new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF}
        );

        short[] array = adapter.readShorts(buffer, 0, 1);
        assertArrayEquals(new short[]{0x010F}, array);
    }

    @Test
    void testReadInts() {

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN);

        ByteBuffer buffer = ByteBuffer.wrap(
                new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFF}
        );

        int[] array = adapter.readInts(buffer, 0, 1);
        assertArrayEquals(new int[]{0x010FF0FF}, array);
    }

    @Test
    void testReadLongs() {

        ArrayBytesAdapter adapter = ArrayBytesAdapter.of(ByteOrder.BIG_ENDIAN);

        ByteBuffer buffer = ByteBuffer.wrap(
                new byte[]{0x01, 0x0F, (byte) 0xF0, (byte) 0xFF, 0x01, 0x0F, (byte) 0xF0, (byte) 0xFF}
        );

        long[] array = adapter.readLongs(buffer, 0, 1);
        assertArrayEquals(new long[]{0x010FF0FF010FF0FFL}, array);
    }
}
