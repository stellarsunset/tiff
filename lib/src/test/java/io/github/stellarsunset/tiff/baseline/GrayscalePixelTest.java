package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Pixel.Grayscale4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GrayscalePixelTest {

    @Test
    void testGrayscale4() {
        assertThrows(IllegalArgumentException.class, () -> new Grayscale4((byte) 16, true), "Out of range: 16");
    }
}
