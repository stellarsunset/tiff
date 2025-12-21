package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Rational;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XResolutionTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(XResolution.TAG.id(), new float[]{1.f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> XResolution.get(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Rational(YResolution.TAG.id(), new int[]{0}, new int[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> XResolution.get(ifd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Rational(XResolution.TAG.id(), new int[]{0}, new int[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(new Rational(0, 1), XResolution.get(ifd));
    }
}
