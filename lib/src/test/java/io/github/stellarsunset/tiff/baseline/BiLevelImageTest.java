package io.github.stellarsunset.tiff.baseline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiLevelImageTest {

    @Test
    void testWhiteIsZero() {
        assertAll(
                () -> assertFalse(new BiLevelImage.Pixel((byte) 0, true).isBlack(), "IsBlack 0"),
                () -> assertTrue(new BiLevelImage.Pixel((byte) 1, true).isBlack(), "IsBlack 1"),
                () -> assertTrue(new BiLevelImage.Pixel((byte) 0, true).isWhite(), "IsWhite 0"),
                () -> assertFalse(new BiLevelImage.Pixel((byte) 1, true).isWhite(), "IsWhite 1")
        );
    }

    @Test
    void testBlackIsZero() {
        assertAll(
                () -> assertTrue(new BiLevelImage.Pixel((byte) 0, false).isBlack(), "IsBlack 0"),
                () -> assertFalse(new BiLevelImage.Pixel((byte) 1, false).isBlack(), "IsBlack 1"),
                () -> assertFalse(new BiLevelImage.Pixel((byte) 0, false).isWhite(), "IsWhite 0"),
                () -> assertTrue(new BiLevelImage.Pixel((byte) 1, false).isWhite(), "IsWhite 1")
        );
    }
}
