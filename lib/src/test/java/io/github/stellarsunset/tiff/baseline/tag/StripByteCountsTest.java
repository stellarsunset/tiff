package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StripByteCountsTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(StripByteCounts.TAG.id(), new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> StripByteCounts.get(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(ImageWidth.TAG.id(), new short[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> StripByteCounts.get(ifd));
    }

    @Test
    void testCorrectShort() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(StripByteCounts.TAG.id(), new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new long[]{8}, StripByteCounts.get(ifd));
    }

    @Test
    void testCorrectLong() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Long(StripByteCounts.TAG.id(), new int[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new long[]{8}, StripByteCounts.get(ifd));
    }
}
