package com.stellarsunset.tiff.extension.geo;

import com.stellarsunset.tiff.Pixel;
import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.Resolution;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.extension.ExtensionImage;

public record GeoImage(ImageDimensions dimensions, StripInfo stripInfo, Resolution resolution,
                       byte[][] data) implements ExtensionImage {
    @Override
    public Pixel valueAt(int x, int y) {
        return Pixel.empty();
    }
}
