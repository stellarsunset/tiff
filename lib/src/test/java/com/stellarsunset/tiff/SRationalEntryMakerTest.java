package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SRationalEntryMakerTest {

    @Test
    void testReader_ValueAtOffset_BE() {

        SeekableByteChannel channel = ByteArrayChannel.fromIntArray(
                new int[]{0x0F, 0xF0, 0x12, 0xFF}
        );

        IfdEntryMaker.SRational maker = new IfdEntryMaker.SRational(
                new BytesReader(channel),
                BytesAdapter.of(ByteOrder.BIG_ENDIAN)
        );

        var entry = maker.makeEntry((short) 0, 2, 0);

        assertAll(
                () -> assertArrayEquals(new int[]{0x0F, 0x12}, entry.numerators(), "Numerators"),
                () -> assertArrayEquals(new int[]{0xF0, 0xFF}, entry.denominators(), "Denominators")
        );
    }

    @Test
    void testReader_ValueAtOffset_LE() {

        SeekableByteChannel channel = ByteArrayChannel.fromIntArray(
                new int[]{0x0F, 0xF0, 0x12, 0xFF}
        );

        IfdEntryMaker.SRational maker = new IfdEntryMaker.SRational(
                new BytesReader(channel),
                BytesAdapter.of(ByteOrder.LITTLE_ENDIAN)
        );

        var entry = maker.makeEntry((short) 0, 2, 0);

        assertAll(
                () -> assertArrayEquals(new int[]{0x0F000000, 0x12000000}, entry.numerators(), "Numerators"),
                () -> assertArrayEquals(new int[]{0xF0000000, 0xFF000000}, entry.denominators(), "Denominators")
        );
    }
}
