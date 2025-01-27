package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Pixel;
import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.extension.FloatImage.Float1Image;
import io.github.stellarsunset.tiff.extension.FloatImage.Float3Image;
import io.github.stellarsunset.tiff.extension.FloatImage.FloatNImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloatImageTest {

    @Test
    void testFloat1Image() {
        Float1Image image = new Float1Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 5)
        );

        assertAll(
                () -> assertEquals(new Pixel.Float1(0), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Pixel.Float1(5), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Pixel.Float1(8), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testFloat3Image() {
        Float3Image image = new Float3Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 15)
        );

        assertAll(
                () -> assertEquals(new Pixel.Float3(0, 1, 2), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Pixel.Float3(11, 12, 13), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Pixel.Float3(16, 17, 18), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testFloatNImage() {
        FloatNImage image = new FloatNImage(
                new ImageDimensions(5, 5),
                5,
                incrementingArray(5, 25)
        );

        assertAll(
                () -> pixelEquals(new Pixel.FloatN(new float[]{0, 1, 2, 3, 4}), image.valueAt(0, 0), "0,0"),
                () -> pixelEquals(new Pixel.FloatN(new float[]{17, 18, 19, 20, 21}), image.valueAt(2, 3), "2,3"),
                () -> pixelEquals(new Pixel.FloatN(new float[]{24, 25, 26, 27, 28}), image.valueAt(4, 4), "4,4")
        );
    }

    private void pixelEquals(Pixel.FloatN expected, Pixel.FloatN actual, String message) {
        assertArrayEquals(expected.values(), actual.values(), message);
    }

    private float[][] incrementingArray(int rows, int columns) {
        float[][] array = new float[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                array[row][col] = row + col;
            }
        }
        return array;
    }
}
