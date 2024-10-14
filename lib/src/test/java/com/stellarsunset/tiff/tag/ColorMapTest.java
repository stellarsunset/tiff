package com.stellarsunset.tiff.tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorMapTest {

    @Test
    void testCreate() {

        short[] shorts = new short[]{
                1, 2, 3, // r
                2, 4, 6, // g
                6, 7, 9  // b
        };

        ColorMap colorMap = ColorMap.create(shorts);
        assertAll(
                "Check parsing of short[]",
                () -> assertEquals(new ColorMap.Rgb((short) 1, (short) 2, (short) 6), colorMap.rgb(0), "Index 0"),
                () -> assertEquals(new ColorMap.Rgb((short) 2, (short) 4, (short) 7), colorMap.rgb(1), "Index 1"),
                () -> assertEquals(new ColorMap.Rgb((short) 3, (short) 6, (short) 9), colorMap.rgb(2), "Index 2")
        );

        assertArrayEquals(shorts, colorMap.flatten(), "Flatten should re-generate the input array.");
    }
}
