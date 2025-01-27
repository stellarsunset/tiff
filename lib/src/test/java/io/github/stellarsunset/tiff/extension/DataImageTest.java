package io.github.stellarsunset.tiff.extension;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataImageTest {

    @Test
    void testCheckAllEqual() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> DataImage.Maker.checkAllEqual(new int[]{1, 2, 1}, "%s")),
                () -> assertDoesNotThrow(() -> DataImage.Maker.checkAllEqual(new int[]{1, 1, 1}, "%s"))
        );
    }
}
