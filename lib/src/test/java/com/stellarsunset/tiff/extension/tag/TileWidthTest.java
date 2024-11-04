package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TileWidthTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(TileWidth.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> TileWidth.getRequired(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(TileLength.ID, new short[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> TileWidth.getRequired(ifd));
    }

    @Test
    void testCorrectShort() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(TileWidth.ID, new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(8, TileWidth.getRequired(ifd));
    }

    @Test
    void testCorrectLong() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Long(TileWidth.ID, new int[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(8, TileWidth.getRequired(ifd));
    }
}
