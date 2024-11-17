package com.stellarsunset.tiff.extension.geokey;

import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * This key is provided to specify the geodetic (geographic or geocentric) coordinate reference system from the GeoTIFF
 * CRS register or to indicate that the Model CRS is a user-defined geodetic coordinate reference system.
 */
public final class GeodeticCrs {

    public static final String NAME = "GEODETIC_CRS";

    public static final short ID = 0x800;

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
