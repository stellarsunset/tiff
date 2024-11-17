package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.Pixel;

public record IntImage() implements ExtensionImage {
    @Override
    public Pixel valueAt(int row, int col) {
        return null;
    }
}
