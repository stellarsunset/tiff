package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTransformationTest {

    @Test
    void testWrongType() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(ModelTransformation.ID, new float[]{1.0f})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(UnsupportedTypeForTagException.class, () -> ModelTransformation.getRequired(ifd));
    }

    @Test
    void testMissingId() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Double(ModelTiepoint.ID, new double[]{1.0d})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(MissingRequiredTagException.class, () -> ModelTransformation.getRequired(ifd));
    }

    @Test
    void testIncorrectNumberOfEntries() {
        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Double(ModelTransformation.ID, new double[]{1.0d})
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        assertThrows(IllegalArgumentException.class, () -> ModelTransformation.getRequired(ifd));
    }

    @Test
    void testCorrect() {
        double[] matrix = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Double(ModelTransformation.ID, matrix)
        };

        Ifd ifd = new Ifd((short) 1, entry, 0);
        double[][] actual = ModelTransformation.getRequired(ifd);

        assertAll(
                () -> assertArrayEquals(new double[]{1, 2, 3, 4}, actual[0], "Row 0"),
                () -> assertArrayEquals(new double[]{5, 6, 7, 8}, actual[1], "Row 1"),
                () -> assertArrayEquals(new double[]{9, 10, 11, 12}, actual[2], "Row 2"),
                () -> assertArrayEquals(new double[]{13, 14, 15, 16}, actual[3], "Row 3")
        );
    }
}
