package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Optional;

/**
 * ModelTiePoints = (...,I,J,K, X,Y,Z...), where (I,J,K) is the point at location (I,J) in raster space with pixel-value
 * K, and (X,Y,Z) is a vector in model space.
 *
 * <p>In most cases the model space is only two-dimensional, in which case both K and Z should be set to zero; this third
 * dimension is provided in anticipation of future support for 3D digital elevation models and vertical coordinate systems.
 *
 * <p>Note: by convention, <i>i</i> is the column in the raster and <i>j</i> is the row.
 */
public record ModelTiepoint(double i, double j, double k, double x, double y, double z) implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x8482, "MODEL_TIE_POINT");

    public static ModelTiepoint[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<ModelTiepoint[]> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalDoubleArray(TAG, ifd).map(ModelTiepoint::createTiepoints);
    }

    static ModelTiepoint[] createTiepoints(double[] doubles) {

        int n = doubles.length / 6;
        ModelTiepoint[] tiepoints = new ModelTiepoint[n];

        for (int i = 0; i < n; i++) {
            int offset = i * 6;
            tiepoints[i] = new ModelTiepoint(
                    doubles[offset],
                    doubles[offset + 1],
                    doubles[offset + 2],
                    doubles[offset + 3],
                    doubles[offset + 4],
                    doubles[offset + 5]
            );
        }

        return tiepoints;
    }
}
