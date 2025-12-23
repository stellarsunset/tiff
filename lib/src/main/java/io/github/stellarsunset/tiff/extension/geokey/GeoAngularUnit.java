package io.github.stellarsunset.tiff.extension.geokey;

import com.google.common.collect.ImmutableMap;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Allows the definition of geocentric CS Linear units for user-defined GCS and for ellipsoids.
 */
public enum GeoAngularUnit implements GeoKey.Accessor {
    RADIAN,
    DEGREE,
    ARC_MINUTE,
    ARC_SECOND,
    GRAD,
    GON,
    DMS,
    DMS_HEMISPHERE;

    public static final GeoKey KEY = new GeoKey((short) 0x806, "GEO_ANGULAR_UNITS");

    private static final Map<Integer, GeoAngularUnit> MAP = ImmutableMap.<Integer, GeoAngularUnit>builder()
            .put(9101, RADIAN)
            .put(9102, DEGREE)
            .put(9103, ARC_MINUTE)
            .put(9104, ARC_SECOND)
            .put(9105, GRAD)
            .put(9106, GON)
            .put(9107, DMS)
            .put(9108, DMS_HEMISPHERE)
            .build();

    public static GeoAngularUnit fromCode(int code) {
        return Optional.ofNullable(MAP.get(code)).orElseThrow(() -> new IllegalArgumentException("Unknown code for a Geometric Angular Unit: " + code));
    }

    public static GeoAngularUnit get(GeoKeyDirectory gkd) {
        return getIfPresent(gkd).orElseThrow(() -> new MissingRequiredGeoKeyException(KEY));
    }

    public static Optional<GeoAngularUnit> getIfPresent(GeoKeyDirectory gkd) {
        OptionalInt code = GeoKey.Accessor.optionalUShort(KEY, gkd);
        return code.isPresent() ? Optional.ofNullable(fromCode(code.getAsInt())) : Optional.empty();
    }
}
