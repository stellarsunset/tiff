package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * Similar to {@link ProjectedCrs} and {@link GeodeticCrs}, this tag is used to describe the vertical datum within an
 * image. Depending on the image content this may be supplied alongside either the projected or geodetic CRS.
 *
 * <p>Take for example a TIFF image where the value of each pixel is an elevation in meters, this field describes the
 * vertical CRS these elevations are with respect to, e.g. a spherical earth, an ellipsoidal model, or a specific geoid.
 */
public final class VerticalCrs implements GeoKey.Accessor {

    public static final GeoKey KEY = new GeoKey((short) 0x1000, "VERTICAL_CRS");

    public static int get(GeoKeyDirectory gkd) {
        return getIfPresent(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(KEY));
    }

    public static OptionalInt getIfPresent(GeoKeyDirectory gkd) {
        return GeoKey.Accessor.optionalUShort(KEY, gkd);
    }
}
