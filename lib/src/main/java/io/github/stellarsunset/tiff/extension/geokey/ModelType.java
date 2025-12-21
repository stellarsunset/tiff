package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * This GeoKey defines the type of Model coordinate reference system used, to which the transformation from the raster
 * space is made.
 *
 * <p>If the Model coordinate reference system is from the GeoTIFF standard CRS register (i.e., EPSG register), then only
 * the registered CRS code need be specified (e.g. in {@link ProjectedCrs} or {@link GeodeticCrs}).
 *
 * <p>If the Model coordinate reference system is not from the GeoTIFF standard CRS register, then it requires specification
 * of some or all CRS elements.
 *
 * <p>The valid values of this field are:
 * <ul>
 *     <li>0 - to indicate that the Model CRS in undefined or unknown</li>
 *     <li>1 - to indicate that the Model CRS is a 2D projected coordinate reference system, indicated by the value of
 *     {@link ProjectedCrs}</li>
 *     <li>2 - to indicate that the Model CRS is a geographic 2D coordinate reference system, indicated by the value of
 *     {@link GeodeticCrs}</li>
 *     <li>3 - to indicate that the Model CRS is a geocentric Cartesian 3D coordinate reference system, indicated by the
 *     value of {@link GeodeticCrs}</li>
 *     <li>32767 - to indicate that the Model CRS type is user-defined/li>
 * </ul>
 *
 * <p>So in general for standard CRSs in the ESPG register this field need not be defined.
 */
public final class ModelType implements GeoKey.Accessor {

    public static GeoKey KEY = new GeoKey((short) 0x400, "MODEL_TYPE");

    public static int get(GeoKeyDirectory gkd) {
        return getIfPresent(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(KEY));
    }

    public static OptionalInt getIfPresent(GeoKeyDirectory gkd) {
        return GeoKey.Accessor.optionalUShort(KEY, gkd);
    }
}
