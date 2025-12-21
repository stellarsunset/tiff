package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * This key is used to specify the projected coordinate reference system from the GeoTIFF CRS register or to indicate
 * that the Model CRS is a user-defined projected coordinate reference system.
 *
 * <p>Projected CRS's convert latitude/longitudes into flat Cartesian-like coordinate systems, usually with units of feet
 * or meters. Using a projected CRS makes it easier to compute distances and areas.
 *
 * <p>E.g. a TIFF image using a projected CRS may have a pixel spacing of 30 meters, 10 meters, etc. so the distances
 * between the pixels remain constant across the image.
 *
 * <p>This is exclusive with {@link GeodeticCrs}.
 */
public final class ProjectedCrs implements GeoKey.Accessor {

    public static final GeoKey KEY = new GeoKey((short) 0xC00, "PROJECTED_CRS");

    public static int get(GeoKeyDirectory gkd) {
        return getIfPresent(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(KEY));
    }

    public static OptionalInt getIfPresent(GeoKeyDirectory gkd) {
        return GeoKey.Accessor.optionalUShort(KEY, gkd);
    }
}
