package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Rational;
import io.github.stellarsunset.tiff.baseline.tag.XResolution;
import io.github.stellarsunset.tiff.baseline.tag.YResolution;

/**
 * Container class for the related baseline tags {@link XResolution} and {@link YResolution} tags.
 */
public record Resolution(Rational xResolution, Rational yResolution) {

    public static Resolution from(Ifd ifd) {
        return new Resolution(XResolution.get(ifd), YResolution.get(ifd));
    }
}
