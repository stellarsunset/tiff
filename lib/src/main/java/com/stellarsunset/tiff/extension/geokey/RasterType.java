package com.stellarsunset.tiff.extension.geokey;

import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.OptionalInt;

/**
 * This requirements class establishes the Raster Space used.
 *
 * <p>There are currently only two options: RasterPixelIsPoint and RasterPixelIsArea. No user-defined raster spaces are
 * currently supported. For variance in imaging display parameters, such as pixel aspect-ratios, use the standard TIFF
 * 6.0 device-space tags.
 * <ul>
 *     <li>0 - to indicate that the Raster type is undefined or unknown</li>
 *     <li>1 - to indicate that the Raster type is PixelIsArea</li>
 *     <li>2 - to indicate that the Raster type is PixelIsPoint</li>
 *     <li>32767 - to indicate that the Raster type is user-defined</li>
 * </ul>
 *
 * <p>Recommendation: the use of 0 (undefined) or 32767 (user-defined) is not recommended
 *
 * <p>The use of this geokey is highly recommended for accurate georeferencing of raster.
 */
public final class RasterType {

    public static final String NAME = "RASTER_TYPE";

    public static final short ID = 0x401;

    public static int getRequired(GeoKeyDirectory gkd) {
        return getOptional(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(NAME, ID));
    }

    public static OptionalInt getOptional(GeoKeyDirectory gkd) {
        return switch (gkd.findKey(ID)) {
            case Entry.Short s -> OptionalInt.of(Short.toUnsignedInt(s.values()[0]));
            case Entry.NotFound _ -> OptionalInt.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForGeoKeyException(NAME, ID);
        };
    }
}
