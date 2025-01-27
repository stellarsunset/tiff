package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.Ifd.Entry;
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
public final class ModelType {

    public static final String NAME = "MODEL_TYPE";

    public static final short ID = 0x400;

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
