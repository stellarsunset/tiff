package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SampleFormatTest {

    @Test
    void testCreateDefault() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(SamplesPerPixel.TAG.id(), new short[]{4})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new int[]{1, 1, 1, 1}, SampleFormat.createDefault(ifd));
    }

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(SampleFormat.TAG.id(), new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> SampleFormat.get(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(SamplesPerPixel.TAG.id(), new short[]{1}),
                new Ifd.Entry.Short(TileWidth.TAG.id(), new short[]{1})
        };

        Ifd ifd = new Ifd((short) 2, entry, 0);
        assertArrayEquals(new int[]{1}, SampleFormat.get(ifd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(SampleFormat.TAG.id(), new short[]{8})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertArrayEquals(new int[]{8}, SampleFormat.get(ifd));
    }
}
