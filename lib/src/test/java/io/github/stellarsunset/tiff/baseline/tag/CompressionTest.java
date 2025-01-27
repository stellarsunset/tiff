package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompressionTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(Compression.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> Compression.get(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(YResolution.ID, new short[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(1, Compression.get(ifd), "Has a default value...");
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(Compression.ID, new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertEquals(8, Compression.get(ifd));
    }
}
