package com.stellarsunset.terrain.tiff.image;

/**
 * Represents the value of an X/Y pixel in an image.
 *
 * <p>Different {@link Image} implementations are expected to return different {@link PixelValue} subtypes specific to
 * their implementation.
 */
public sealed interface PixelValue {

    /**
     * Represents an empty pixel value.
     *
     * <p>Expected as a potential response type for out-of-range queries, testing, etc.
     */
    record Empty() implements PixelValue {
    }

    /**
     * {@link PixelValue} subtype for use in {@link Image.Baseline} images.
     */
    sealed interface Baseline extends PixelValue {
    }

    record BlackOrWhite() implements PixelValue.Baseline {
    }

    record Grayscale() implements PixelValue.Baseline {
    }

    record PaletteColor() implements PixelValue.Baseline {
    }

    record Rgb() implements PixelValue.Baseline {
    }

    /**
     * {@link PixelValue} subtype for use in {@link Image.Extension} images.
     */
    non-sealed interface Extension extends PixelValue {

    }
}
