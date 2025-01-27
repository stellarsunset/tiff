package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import io.github.stellarsunset.tiff.baseline.tag.YResolution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoKeyDirectoryTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(GeoKeyDirectory.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> GeoKeyDirectory.getRequired(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(YResolution.ID, new short[]{1})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> GeoKeyDirectory.getRequired(ifd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entries = new Ifd.Entry[]{
                new Ifd.Entry.Short(
                        GeoKeyDirectory.ID,
                        new short[]{
                                1, 1, 0, 1, // header
                                1, 0, 1, 0  // an entry
                        }
                )
        };

        Ifd ifd = new Ifd((short) 1, entries, 0);

        GeoKeyDirectory gkd = GeoKeyDirectory.getRequired(ifd);
        Ifd.Entry entry = gkd.findKey((short) 1);

        assertAll(
                () -> assertEquals(1, gkd.numberOfKeys(), "Should be one key"),
                () -> assertEquals(1, entry.tag(), "Entry tag should be 1"),
                () -> assertInstanceOf(Ifd.Entry.Short.class, entry, "Should be a short entry")
        );
    }
}
