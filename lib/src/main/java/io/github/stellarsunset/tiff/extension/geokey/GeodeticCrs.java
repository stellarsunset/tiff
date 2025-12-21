package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * This key is provided to specify the geodetic (geographic or geocentric) coordinate reference system from the GeoTIFF
 * CRS register or to indicate that the Model CRS is a user-defined geodetic coordinate reference system.
 *
 * <p>Geodetic CRS's use latitude and longitude measured in degrees, typically on a spherical or ellipsoidal model of
 * the Earth. Using a geodetic CRS gives a globally-consistent projection and preserves angles.
 *
 * <p>However they're bad for measuring distances and areas, as the distance between from one line of longitude to another,
 * e.g. in meters, changes at different latitudes so the physical spacing between pixel measurements isn't consistent.
 *
 * <p>E.g. a TIFF image using a geodetic CRS may have a pixel spacing of 1 arc-second.
 *
 * <p>This is exclusive with {@link ProjectedCrs}. Some interpretation help is given in {@link ModelType}.
 */
public final class GeodeticCrs implements GeoKey.Accessor {

    public static final GeoKey KEY = new GeoKey((short) 0x800, "GEODETIC_CRS");

    public static int get(GeoKeyDirectory gkd) {
        return getIfPresent(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(KEY));
    }

    public static OptionalInt getIfPresent(GeoKeyDirectory gkd) {
        return GeoKey.Accessor.optionalUShort(KEY, gkd);
    }
}
