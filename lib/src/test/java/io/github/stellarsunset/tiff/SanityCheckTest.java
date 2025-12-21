package io.github.stellarsunset.tiff;

import io.github.stellarsunset.tiff.baseline.*;
import io.github.stellarsunset.tiff.extension.ExtensionImage;
import io.github.stellarsunset.tiff.extension.FloatImage;
import io.github.stellarsunset.tiff.extension.IntImage;
import io.github.stellarsunset.tiff.extension.ShortImage;
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
                switch (extensionImage) {
                    case ShortImage s -> {
                    }
                    case IntImage i -> {
                    }
                    case FloatImage f -> {
                    }
                    default -> {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
    }

    @Test
    void testPixelHierarchy() {
        Image.Pixel pixel = Image.Pixel.empty();
        switch (pixel) {
            case BaselineImage.Pixel baseline -> {
                switch (baseline) {
                    case BiLevelImage.Pixel blackOrWhite -> {
                    }
                    case GrayscaleImage.FourBit.Pixel grayscale4 -> {
                    }
                    case GrayscaleImage.EightBit.Pixel grayscale8 -> {
                    }
                    case PaletteColorImage.Pixel paletteColor -> {
                    }
                    case RgbImage.Pixel rgb -> {
                    }
                }
            }
            case Image.Pixel.Empty empty -> {
            }
            case ExtensionImage.Pixel extension -> {
            }
        }
    }
}
