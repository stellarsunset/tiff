package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.Pixel.BlackOrWhite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlackOrWhitePixelTest {

    @Test
    void testWhiteIsZero() {
        assertAll(
                () -> assertFalse(new BlackOrWhite((byte) 0, true).isBlack(), "IsBlack 0"),
                () -> assertTrue(new BlackOrWhite((byte) 1, true).isBlack(), "IsBlack 1"),
                () -> assertTrue(new BlackOrWhite((byte) 0, true).isWhite(), "IsWhite 0"),
                () -> assertFalse(new BlackOrWhite((byte) 1, true).isWhite(), "IsWhite 1")
        );
    }

    @Test
    void testBlackIsZero() {
        assertAll(
                () -> assertTrue(new BlackOrWhite((byte) 0, false).isBlack(), "IsBlack 0"),
                () -> assertFalse(new BlackOrWhite((byte) 1, false).isBlack(), "IsBlack 1"),
                () -> assertFalse(new BlackOrWhite((byte) 0, false).isWhite(), "IsWhite 0"),
                () -> assertTrue(new BlackOrWhite((byte) 1, false).isWhite(), "IsWhite 1")
        );
    }
}
