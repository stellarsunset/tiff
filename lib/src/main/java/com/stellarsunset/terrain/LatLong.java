package com.stellarsunset.terrain;

import static com.google.common.base.Preconditions.checkArgument;

public record LatLong(double latitude, double longitude) {

    public LatLong {
        checkArgument(-90 <= latitude && latitude <= 90, "Latitude should be in range [-90, 90]");
        checkArgument(-180 <= longitude && longitude <= 180, "Longitude should be in range [-180, 180]");
    }
}
