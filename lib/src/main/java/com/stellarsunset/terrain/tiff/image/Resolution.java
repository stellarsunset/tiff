package com.stellarsunset.terrain.tiff.image;

import com.stellarsunset.terrain.tiff.Ifd;
import com.stellarsunset.terrain.tiff.Rational;
import com.stellarsunset.terrain.tiff.tag.XResolution;
import com.stellarsunset.terrain.tiff.tag.YResolution;

/**
 * Container class for the related baseline tags {@link XResolution} and {@link YResolution} tags.
 */
public record Resolution(Rational xResolution, Rational yResolution) {

    public static Resolution from(Ifd ifd) {
        return new Resolution(XResolution.getRequired(ifd), YResolution.getRequired(ifd));
    }
}
