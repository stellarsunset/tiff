package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.StripByteCounts;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TileByteCountsTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(TileByteCounts.TAG.id(), new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> TileByteCounts.get(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(StripByteCounts.TAG.id(), new short[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> TileByteCounts.get(ifd));
    }

    @Test
    void testCorrectShort() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(TileByteCounts.TAG.id(), new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new long[]{8}, TileByteCounts.get(ifd));
    }

    @Test
    void testCorrectLong() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Long(TileByteCounts.TAG.id(), new int[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new long[]{8}, TileByteCounts.get(ifd));
    }
}
