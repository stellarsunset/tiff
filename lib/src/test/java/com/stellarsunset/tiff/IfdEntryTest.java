package com.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class IfdEntryTest {

    @Test
    void testCompareTo_IsUnsigned() {

        Ifd.Entry[] entries = new Ifd.Entry[]{
                Ifd.Entry.notFound((short) -1),
                Ifd.Entry.notFound((short) 0),
                Ifd.Entry.notFound((short) 1)
        };
        Arrays.sort(entries);

        short[] expected = new short[]{0, 1, -1};
        short[] actual = new short[]{entries[0].tag(), entries[1].tag(), entries[2].tag()};

        assertArrayEquals(expected, actual);
    }

    @Test
    void testRational() {
        assertAll(
                () -> assertDoesNotThrow(() -> new Ifd.Entry.Rational((short) 1, new int[]{1}, new int[]{2}),
                        "Should not throw on same length arrays"),
                () -> assertThrows(IllegalArgumentException.class, () -> new Ifd.Entry.Rational((short) 1, new int[]{1, 2}, new int[]{2}),
                        "Should throw on different length arrays")
        );
    }

    @Test
    void testSRational() {
        assertAll(
                () -> assertDoesNotThrow(() -> new Ifd.Entry.SRational((short) 1, new int[]{1}, new int[]{2}),
                        "Should not throw on same length arrays"),
                () -> assertThrows(IllegalArgumentException.class, () -> new Ifd.Entry.SRational((short) 1, new int[]{1, 2}, new int[]{2}),
                        "Should throw on different length arrays")
        );
    }
}
