package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Rational;
import com.stellarsunset.tiff.tag.XResolution;
import com.stellarsunset.tiff.tag.YResolution;

/**
 * Container class for the related baseline tags {@link XResolution} and {@link YResolution} tags.
 */
public record Resolution(Rational xResolution, Rational yResolution) {

    public static Resolution from(Ifd ifd) {
        return new Resolution(XResolution.getRequired(ifd), YResolution.getRequired(ifd));
    }
}
