package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitsPerSampleTest {

    @Test
    void testCreateDefault() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(SamplesPerPixel.ID, new short[]{4})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new int[]{1, 1, 1, 1}, BitsPerSample.createDefault(ifd));
    }

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(BitsPerSample.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> BitsPerSample.getRequired(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(SamplesPerPixel.ID, new short[]{1}),
                new Ifd.Entry.Short(YResolution.ID, new short[]{8})
        };

        Ifd ifd = new Ifd((short) 2, entry, 0);
        assertArrayEquals(new int[]{1}, BitsPerSample.getRequired(ifd), "Has a default value...");
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(BitsPerSample.ID, new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new int[]{8}, BitsPerSample.getRequired(ifd));
    }
}
