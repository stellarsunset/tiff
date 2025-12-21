package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.extension.ShortImage.Short1Image;
import io.github.stellarsunset.tiff.extension.ShortImage.Short3Image;
import io.github.stellarsunset.tiff.extension.ShortImage.ShortNImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortImageTest {

    @Test
    void testShort1Image() {
        Short1Image image = new Short1Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 5)
        );

        assertAll(
                () -> assertEquals(new Short1Image.Pixel((short) 0), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Short1Image.Pixel((short) 5), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Short1Image.Pixel((short) 8), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testShort3Image() {
        Short3Image image = new Short3Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 15)
        );

        assertAll(
                () -> assertEquals(new Short3Image.Pixel((short) 0, (short) 1, (short) 2), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Short3Image.Pixel((short) 11, (short) 12, (short) 13), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Short3Image.Pixel((short) 16, (short) 17, (short) 18), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testShortNImage() {
        ShortNImage image = new ShortNImage(
                new ImageDimensions(5, 5),
                5,
                incrementingArray(5, 25)
        );

        assertAll(
                () -> pixelEquals(new ShortNImage.Pixel(new short[]{0, 1, 2, 3, 4}), image.valueAt(0, 0), "0,0"),
                () -> pixelEquals(new ShortNImage.Pixel(new short[]{17, 18, 19, 20, 21}), image.valueAt(2, 3), "2,3"),
                () -> pixelEquals(new ShortNImage.Pixel(new short[]{24, 25, 26, 27, 28}), image.valueAt(4, 4), "4,4")
        );
    }

    private void pixelEquals(ShortNImage.Pixel expected, ShortNImage.Pixel actual, String message) {
        assertArrayEquals(expected.values(), actual.values(), message);
    }

    private short[][] incrementingArray(int rows, int columns) {
        short[][] array = new short[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                array[row][col] = (short) (row + col);
            }
        }
        return array;
    }
}
