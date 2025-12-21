package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Optional;

/**
 * A positive ScaleX in the ModelPixelScaleTag SHALL indicate that model space X coordinates increase as raster space I
 * indices increase. This is the standard horizontal relationship between raster space and model space.
 *
 * <p>A positive ScaleY in the ModelPixelScaleTag SHALL indicate that model space Y coordinates decrease as raster space
 * J indices increase. This is the standard vertical relationship between raster space and model space.
 *
 * <p>The ScaleZ is primarily used to map the pixel value of a digital elevation model into the correct Z-scale (in other
 * words a Z-Scaling factor).
 *
 * <p>Simple reversals of orientation from the standard relationship between raster and model space (e.g., horizontal or
 * vertical flips) SHALL be indicated by reversal of sign in the corresponding component of the ModelPixelScaleTag.
 *
 * <p>GeoTIFF compliant readers shall honor this sign-reversal convention.
 */
public record ModelPixelScale(double x, double y, double z) implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x830E, "MODEL_PIXEL_SCALE");

    public static ModelPixelScale getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<ModelPixelScale> getOptional(Ifd ifd) {
        return Tag.Accessor.optionalDoubleArray(TAG, ifd).map(d -> new ModelPixelScale(d[0], d[1], d[2]));
    }
}
