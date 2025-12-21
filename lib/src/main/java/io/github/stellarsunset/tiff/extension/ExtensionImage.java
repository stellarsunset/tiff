package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.baseline.BaselineImage;

/**
 * Interface to extension image types that are TIFF-compliant but defined outside the baseline TIFF 6.0 specification,
 * see {@link BaselineImage}.
 *
 * <p>This is provided as both:
 * <ol>
 *     <li>An extension point for non-baseline TIFF file types supported in library</li>
 *     <li>An extension point for external libraries</li>
 * </ol>
 *
 * <p>In-library we define "extension image" types for images containing data in their pixel values. The interpretation
 * of the data stored in the various pixels depends on the type of image (e.g. might be elevation data).
 */
public non-sealed interface ExtensionImage extends Image {

    @Override
    Pixel valueAt(int row, int col);

    /**
     * {@link Pixel} subtype for use in {@link ExtensionImage} images.
     *
     * <p>Provided to allow external libraries to add their own pixel types without losing the sealed baseline type
     * hierarchy.
     */
    non-sealed interface Pixel extends Image.Pixel {
    }
}
