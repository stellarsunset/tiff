package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.extension.ByteImage.Byte1Image;
import io.github.stellarsunset.tiff.extension.ByteImage.Byte3Image;
import io.github.stellarsunset.tiff.extension.ByteImage.ByteNImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ByteImageTest {

    @Test
    void testShort1Image() {
        Byte1Image image = new Byte1Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 5)
        );

        assertAll(
                () -> assertEquals(new Byte1Image.Pixel((byte) 0), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Byte1Image.Pixel((byte) 5), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Byte1Image.Pixel((byte) 8), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testShort3Image() {
        Byte3Image image = new Byte3Image(
                new ImageDimensions(5L, 5L),
                incrementingArray(5, 15)
        );

        assertAll(
                () -> assertEquals(new Byte3Image.Pixel((byte) 0, (byte) 1, (byte) 2), image.valueAt(0, 0), "0,0"),
                () -> assertEquals(new Byte3Image.Pixel((byte) 11, (byte) 12, (byte) 13), image.valueAt(2, 3), "2,3"),
                () -> assertEquals(new Byte3Image.Pixel((byte) 16, (byte) 17, (byte) 18), image.valueAt(4, 4), "4,4")
        );
    }

    @Test
    void testShortNImage() {
        ByteNImage image = new ByteNImage(
                new ImageDimensions(5, 5),
                5,
                incrementingArray(5, 25)
        );

        assertAll(
                () -> pixelEquals(new ByteNImage.Pixel(new byte[]{0, 1, 2, 3, 4}), image.valueAt(0, 0), "0,0"),
                () -> pixelEquals(new ByteNImage.Pixel(new byte[]{17, 18, 19, 20, 21}), image.valueAt(2, 3), "2,3"),
                () -> pixelEquals(new ByteNImage.Pixel(new byte[]{24, 25, 26, 27, 28}), image.valueAt(4, 4), "4,4")
        );
    }

    private void pixelEquals(ByteNImage.Pixel expected, ByteNImage.Pixel actual, String message) {
        assertArrayEquals(expected.values(), actual.values(), message);
    }

    private byte[][] incrementingArray(int rows, int columns) {
        byte[][] array = new byte[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                array[row][col] = (byte) (row + col);
            }
        }
        return array;
    }
}
