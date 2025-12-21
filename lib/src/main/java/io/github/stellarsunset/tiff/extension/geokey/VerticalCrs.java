package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * Similar to {@link ProjectedCrs} and {@link GeodeticCrs}, this tag is used to describe the vertical datum within an
 * image. Depending on the image content this may be supplied alongside either the projected or geodetic CRS.
 *
 * <p>Take for example a TIFF image where the value of each pixel is an elevation in meters, this field describes the
 * vertical CRS these elevations are with respect to, e.g. a spherical earth, an ellipsoidal model, or a specific geoid.
 */
public final class VerticalCrs {

    public static final String NAME = "VERTICAL_CRS";

    public static final short ID = 0x1000;

    public static int getRequired(GeoKeyDirectory gkd) {
        return getOptional(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(NAME, ID));
    }

    public static OptionalInt getOptional(GeoKeyDirectory gkd) {
        return switch (gkd.findKey(ID)) {
            case Entry.Short s -> OptionalInt.of(Short.toUnsignedInt(s.values()[0]));
            case Entry.NotFound _ -> OptionalInt.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                    throw new UnsupportedTypeForGeoKeyException(NAME, ID);
        };
    }
}
