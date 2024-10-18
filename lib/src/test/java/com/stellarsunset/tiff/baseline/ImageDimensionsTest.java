package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.ImageLength;
import com.stellarsunset.tiff.baseline.tag.ImageWidth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageDimensionsTest {

    @Test
    void testFrom() {

        Ifd ifd = new Ifd(
                (short) 2,
                new Entry[]{
                        new Entry.Short(ImageWidth.ID, new short[]{10}),
                        new Entry.Short(ImageLength.ID, new short[]{20})
                },
                0
        );

        ImageDimensions dimensions = ImageDimensions.from(ifd);
        assertAll(
                () -> assertEquals(10, dimensions.imageWidth(), "Image Width"),
                () -> assertEquals(20, dimensions.imageLength(), "Image Length"),
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

        ImageDimensions dimensions = ImageDimensions.from(ifd);
        assertAll(
                () -> assertEquals(4294967295L, dimensions.imageWidth(), "Image Width"),
                () -> assertEquals(4294967295L, dimensions.imageLength(), "Image Length"),
                () -> assertThrows(IllegalArgumentException.class, dimensions::asIntInfo, "Should fail on narrowing to integer values.")
        );
    }

    @Test
    void testCheckBounds() {
        ImageDimensions dimensions = new ImageDimensions(10, 10);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> dimensions.checkBounds(new byte[5][5], 1), "[5][5], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new byte[10][10], 1), "[10][10], 1"),
                () -> assertDoesNotThrow(() -> dimensions.checkBounds(new byte[10][30], 3), "[10][30], 3")
        );
    }
}
