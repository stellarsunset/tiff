package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GkdEntryMakerTest {

    @Test
    void testMaker_ValueInOffset() {

        Ifd ifd = new Ifd((short) 0, new Ifd.Entry[0], 0);

        GkdEntryMaker maker = new GkdEntryMaker(ifd);

        Ifd.Entry entry = maker.makeEntry(
                (short) 1,
                (short) 0,
                (short) 1,
                (short) 5
        );

        if (entry instanceof Ifd.Entry.Short s) {
            assertAll(
                    () -> assertEquals(1, entry.tag(), "Tag should be the GeoKey ID"),
                    () -> assertArrayEquals(new short[]{5}, s.values(), "Offset should be the entry value")
            );
        } else {
            Assertions.fail("Entry of incorrect type, expected short got: " + entry.getClass());
        }
    }

    @Test
    void testMaker_ValueInTag() {

        Ifd ifd = new Ifd(
                (short) 2,
                new Ifd.Entry[]{
                        new Ifd.Entry.Short((short) 1, new short[]{6}),
                        new Ifd.Entry.Long((short) 2, new int[]{10, 11, 12})
                },
                0
        );

        GkdEntryMaker maker = new GkdEntryMaker(ifd);

        Ifd.Entry sEntry = maker.makeEntry(
                (short) 3,
                (short) 1,
                (short) 1,
                (short) 0
        );

        if (sEntry instanceof Ifd.Entry.Short s) {
            assertAll(
                    "Single value in tag extraction",
                    () -> assertEquals(3, s.tag(), "Tag should be the GeoKey ID"),
                    () -> assertArrayEquals(new short[]{6}, s.values(), "Values should match values of tag with ID 1")
            );
        } else {
            Assertions.fail("Entry of incorrect type, expected short got: " + sEntry.getClass());
        }

        Ifd.Entry lEntry = maker.makeEntry(
                (short) 6,
                (short) 2,
                (short) 2,
                (short) 1
        );

        if (lEntry instanceof Ifd.Entry.Long l) {
            assertAll(
                    "Single value in tag extraction",
                    () -> assertEquals(6, l.tag(), "Tag should be the GeoKey ID"),
                    () -> assertArrayEquals(new int[]{11, 12}, l.values(), "Values should match range [1, 3) values of tag with ID 2")
            );
        } else {
            Assertions.fail("Entry of incorrect type, expected short got: " + sEntry.getClass());
        }
    }

    @Test
    void testMaker_MakeAllEntries() {

        Ifd ifd = new Ifd((short) 0, new Ifd.Entry[0], 0);

        GkdEntryMaker maker = new GkdEntryMaker(ifd);

        Ifd.Entry[] entries = maker.makeAllEntries(
                new short[]{
                        1, 0, 1, 4,
                        5, 0, 1, 8
                },
                0
        );

        assertEquals(2, entries.length, "Should product two entries.");

        if (entries[0] instanceof Ifd.Entry.Short e1 && entries[1] instanceof Ifd.Entry.Short e2) {
            assertAll(
                    () -> assertEquals(1, e1.tag(), "E1 Tag"),
                    () -> assertArrayEquals(new short[]{4}, e1.values(), "E1 Values"),

                    () -> assertEquals(5, e2.tag(), "E2 Tag"),
                    () -> assertArrayEquals(new short[]{8}, e2.values(), "E2 Values")
            );
        } else {
            Assertions.fail(
                    String.format(
                            "Entries not of correct type short, got: %s and %s.",
                            entries[0].getClass(),
                            entries[1].getClass()
                    )
            );
        }
    }
}
