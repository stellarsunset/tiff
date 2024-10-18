package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.image.PixelValue.Grayscale4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GrayscalePixelValueTest {

    @Test
    void testGrayscale4() {
        assertThrows(IllegalArgumentException.class, () -> new Grayscale4((byte) 16, true), "Out of range: 16");
    }
}
