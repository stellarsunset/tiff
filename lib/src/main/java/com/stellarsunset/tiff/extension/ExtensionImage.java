package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.baseline.BaselineImage;

/**
 * Interface to extension image types that are TIFF-compliant but defined outside the baseline TIFF 6.0 specification,
 * see {@link BaselineImage}.
 *
 * <p>This is provided as both:
 * <ol>
 *     <li>An extension point for non-baseline TIFF files types supported in library</li>
 *     <li>An extension point for external libraries</li>
 * </ol>
 */
public non-sealed interface ExtensionImage extends Image {
}
