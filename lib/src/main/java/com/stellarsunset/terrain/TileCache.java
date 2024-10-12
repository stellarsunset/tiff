package com.stellarsunset.terrain;

public interface TileCache {

    /**
     * Returns the appropriate {@link TerrainTile} to query for elevations at the given {@link LatLong}.
     *
     * <p>If there is no valid tile in the cache.
     */
    Either<TerrainTile, Error> tileFor(LatLong latLong);

    sealed interface Error {

        RuntimeException asException();

        record NoSuchTile(LatLong latLong) implements Error {
            @Override
            public RuntimeException asException() {
                return new IllegalArgumentException();
            }
        }
    }
}
