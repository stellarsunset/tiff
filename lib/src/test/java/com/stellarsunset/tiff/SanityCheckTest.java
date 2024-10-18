package com.stellarsunset.tiff;

import com.stellarsunset.tiff.baseline.*;
import com.stellarsunset.tiff.extension.ExtensionImage;
import org.junit.jupiter.api.Test;

/**
 * Need to keep my brain straight looking at the class hierarchy... want to make sure this doesn't get too crazy...
 */
class SanityCheckTest {

    @Test
    void testImageHierarchy() {
        Image image = Image.unknown(null, null);
        switch (image) {
            case Image.Lazy lazy -> {
            }
            case Image.Unknown unknown -> {
            }
            case BaselineImage baselineImage -> {
                switch (baselineImage) {
                    case BiLevelImage biLevelImage -> {
                    }
                    case GrayscaleImage grayscaleImage -> {
                    }
                    case PaletteColorImage paletteColorImage -> {
                    }
                    case RgbImage rgbImage -> {
                    }
                }
            }
            case ExtensionImage extensionImage -> {
            }
        }
    }

    @Test
    void testPixelHierarchy() {
        Pixel pixel = Pixel.empty();
        switch (pixel) {
            case Pixel.Baseline baseline -> {
                switch (baseline) {
                    case Pixel.BlackOrWhite blackOrWhite -> {
                    }
                    case Pixel.Grayscale4 grayscale4 -> {
                    }
                    case Pixel.Grayscale8 grayscale8 -> {
                    }
                    case Pixel.PaletteColor paletteColor -> {
                    }
                    case Pixel.Rgb rgb -> {
                    }
                }
            }
            case Pixel.Empty empty -> {
            }
            case Pixel.Extension extension -> {
            }
        }
    }
}
