package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Pixel;
import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.extension.IntImage.Int1Image;
import io.github.stellarsunset.tiff.extension.IntImage.Int3Image;
import io.github.stellarsunset.tiff.extension.IntImage.IntNImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntImageTest {

    @Test
    void testInt1Image() {
        Int1Image image = new Int1Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 5)
        );

        assertAll(
                () -> assertEquals(new Pixel.Int1(0), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Pixel.Int1(5), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Pixel.Int1(8), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testInt3Image() {
        Int3Image image = new Int3Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 15)
        );

        assertAll(
                () -> assertEquals(new Pixel.Int3(0, 1, 2), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Pixel.Int3(11, 12, 13), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Pixel.Int3(16, 17, 18), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testIntNImage() {
        IntNImage image = new IntNImage(
                new ImageDimensions(5, 5),
                5,
                incrementingArray(5, 25)
        );

        assertAll(
                () -> pixelEquals(new Pixel.IntN(new int[]{0, 1, 2, 3, 4}), image.valueAt(0, 0), "0,0"),
                () -> pixelEquals(new Pixel.IntN(new int[]{17, 18, 19, 20, 21}), image.valueAt(2, 3), "2,3"),
                () -> pixelEquals(new Pixel.IntN(new int[]{24, 25, 26, 27, 28}), image.valueAt(4, 4), "4,4")
        );
    }

    private void pixelEquals(Pixel.IntN expected, Pixel.IntN actual, String message) {
        assertArrayEquals(expected.values(), actual.values(), message);
    }

    private int[][] incrementingArray(int rows, int columns) {
        int[][] array = new int[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                array[row][col] = row + col;
            }
        }
        return array;
    }
}
