package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import com.stellarsunset.tiff.baseline.tag.YResolution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTiepointTest {

    @Test
    void testWrongType() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(ModelTiepoint.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> ModelTiepoint.getRequired(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Double(YResolution.ID, new double[]{1.0})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> ModelTiepoint.getRequired(ifd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entries = new Ifd.Entry[]{
                new Ifd.Entry.Double(
                        ModelTiepoint.ID,
                        new double[]{
                                0.0, 1.0, 2.0, // i,j,k
                                3.0, 4.0, 5.0  // x,y,z
                        }
                )
        };

        Ifd ifd = new Ifd((short) 1, entries, 0);

        ModelTiepoint[] tiepoints = ModelTiepoint.getRequired(ifd);
        assertEquals(1, tiepoints.length, "Should have one tiepoint");

        ModelTiepoint tiepoint = tiepoints[0];
        assertAll(
                () -> assertEquals(0.0, tiepoint.i(), "i"),
                () -> assertEquals(1.0, tiepoint.j(), "j"),
                () -> assertEquals(2.0, tiepoint.k(), "k"),
                () -> assertEquals(3.0, tiepoint.x(), "x"),
                () -> assertEquals(4.0, tiepoint.y(), "y"),
                () -> assertEquals(5.0, tiepoint.z(), "z")
        );
    }
}
