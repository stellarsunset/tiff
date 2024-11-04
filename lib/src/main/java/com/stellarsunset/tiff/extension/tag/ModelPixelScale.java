package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

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
public record ModelPixelScale(double x, double y, double z) {

    public static final String NAME = "MODEL_PIXEL_SCALE";

    public static final short ID = (short) 0x33550;

    public static ModelPixelScale getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<ModelPixelScale> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Double d -> Optional.of(new ModelPixelScale(d.values()[0], d.values()[1], d.values()[2]));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                 Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
