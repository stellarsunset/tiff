package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.util.Optional;

/**
 * ModelTiePoints = (...,I,J,K, X,Y,Z...), where (I,J,K) is the point at location (I,J) in raster space with pixel-value
 * K, and (X,Y,Z) is a vector in model space.
 *
 * <p>In most cases the model space is only two-dimensional, in which case both K and Z should be set to zero; this third
 * dimension is provided in anticipation of future support for 3D digital elevation models and vertical coordinate systems.
 */
public record ModelTiepoint(double i, double j, double k, double x, double y, double z) {

    public static final String NAME = "MODEL_TIE_POINT";

    public static final short ID = (short) 0x8482;

    public static ModelTiepoint[] getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<ModelTiepoint[]> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Double d -> Optional.of(createTiepoints(d.values()));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                 Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
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
