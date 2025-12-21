package io.github.stellarsunset.tiff.baseline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GrayscaleImageTest {

    @Test
    void testGrayscale4() {
        assertThrows(IllegalArgumentException.class, () -> new GrayscaleImage.FourBit.Pixel((byte) 16, true), "Out of range: 16");
    }
}
