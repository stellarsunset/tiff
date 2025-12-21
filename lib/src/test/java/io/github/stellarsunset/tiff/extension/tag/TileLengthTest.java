package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TileLengthTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(TileLength.TAG.id(), new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> TileLength.get(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(TileWidth.TAG.id(), new short[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> TileLength.get(ifd));
    }

    @Test
    void testCorrectShort() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(TileLength.TAG.id(), new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(8, TileLength.get(ifd));
    }

    @Test
    void testCorrectLong() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Long(TileLength.TAG.id(), new int[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(8, TileLength.get(ifd));
    }
}
