package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.ImageLength;
import io.github.stellarsunset.tiff.baseline.tag.ImageWidth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageDimensionsTest {

    @Test
    void testGet() {

        Ifd ifd = new Ifd(
                (short) 2,
                new Entry[]{
                        new Entry.Short(ImageWidth.ID, new short[]{10}),
                        new Entry.Short(ImageLength.ID, new short[]{20})
                },
                0
        );

        ImageDimensions dimensions = ImageDimensions.get(ifd);
        assertAll(
                () -> assertEquals(10, dimensions.width(), "Image Width"),
                () -> assertEquals(20, dimensions.length(), "Image Length"),
                () -> assertDoesNotThrow(dimensions::asIntInfo, "Should be able to narrow to integer values.")
        );
    }

    @Test
    void testAsIntInfo() {

        Ifd ifd = new Ifd(
                (short) 2,
                new Entry[]{
                        new Entry.Long(ImageWidth.ID, new int[]{-1}),
                        new Entry.Long(ImageLength.ID, new int[]{-1})
                },
                0
        );

        ImageDimensions dimensions = ImageDimensions.get(ifd);
        assertAll(
                () -> assertEquals(4294967295L, dimensions.width(), "Image Width"),
                () -> assertEquals(4294967295L, dimensions.length(), "Image Length"),
                () -> assertThrows(IllegalArgumentException.class, dimensions::asIntInfo, "Should fail on narrowing to integer values.")
        );
    }

    @Test
    void testCheckBoundsBytes() {
        ImageDimensions dimensions = new ImageDimensions(10, 10);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> dimensions.checkBounds(new byte[5][5], 1), "[5][5], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new byte[10][10], 1), "[10][10], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new byte[10][30], 3), "[10][30], 3")
        );
    }

    @Test
    void testCheckBoundsShorts() {
        ImageDimensions dimensions = new ImageDimensions(10, 10);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> dimensions.checkBounds(new short[5][5], 1), "[5][5], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new short[10][10], 1), "[10][10], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new short[10][30], 3), "[10][30], 3")
        );
    }

    @Test
    void testCheckBoundsInts() {
        ImageDimensions dimensions = new ImageDimensions(10, 10);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> dimensions.checkBounds(new byte[5][5], 1), "[5][5], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new int[10][10], 1), "[10][10], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new int[10][30], 3), "[10][30], 3")
        );
    }

    @Test
    void testCheckBoundsFloats() {
        ImageDimensions dimensions = new ImageDimensions(10, 10);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> dimensions.checkBounds(new float[5][5], 1), "[5][5], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new float[10][10], 1), "[10][10], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new float[10][30], 3), "[10][30], 3")
        );
    }
}
