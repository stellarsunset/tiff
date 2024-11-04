package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import com.stellarsunset.tiff.baseline.tag.YResolution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelPixelScaleTest {

    @Test
    void testWrongType() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(ModelPixelScale.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> ModelPixelScale.getRequired(ifd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Double(YResolution.ID, new double[]{1.0})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> ModelPixelScale.getRequired(ifd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entries = new Ifd.Entry[]{
                new Ifd.Entry.Double(
                        ModelPixelScale.ID,
                        new double[]{
                                0.0, 1.0, 2.0, // x,y,z
                        }
                )
        };

        Ifd ifd = new Ifd((short) 1, entries, 0);

        ModelPixelScale scale = ModelPixelScale.getRequired(ifd);
        assertAll(
                () -> assertEquals(0.0, scale.x(), "x"),
                () -> assertEquals(1.0, scale.y(), "y"),
                () -> assertEquals(2.0, scale.z(), "z")
        );
    }
}
