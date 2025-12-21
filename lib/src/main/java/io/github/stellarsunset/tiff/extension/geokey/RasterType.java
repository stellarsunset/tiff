package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

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
public final class RasterType implements GeoKey.Accessor {

    public static final GeoKey KEY = new GeoKey((short) 0x401, "RASTER_TYPE");

    public static int get(GeoKeyDirectory gkd) {
        return getIfPresent(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(KEY));
    }

    public static OptionalInt getIfPresent(GeoKeyDirectory gkd) {
        return GeoKey.Accessor.optionalUShort(KEY, gkd);
    }
}
