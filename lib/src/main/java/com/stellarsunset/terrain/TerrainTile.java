package com.stellarsunset.terrain;

/**
 * Handle for a terrain data tile that supports elevation look-ups at {@link LatLong} locations.
 */
public interface TerrainTile extends AutoCloseable {

    Either<Elevations, Error> elevationsFor(LatLong location);

    sealed interface Error {

        RuntimeException asException();

        record OutOfRange(LatLong latLong) implements Error {

            @Override
            public RuntimeException asException() {
                return new IllegalArgumentException();
            }
        }
    }
}
