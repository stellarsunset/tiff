package com.stellarsunset.tiff.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Rational;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YResolutionTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(YResolution.ID, new float[]{1.f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> YResolution.getRequired(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Rational(XResolution.ID, new int[]{0}, new int[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> YResolution.getRequired(ifd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Rational(YResolution.ID, new int[]{0}, new int[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(new Rational(0, 1), YResolution.getRequired(ifd));
    }
}
