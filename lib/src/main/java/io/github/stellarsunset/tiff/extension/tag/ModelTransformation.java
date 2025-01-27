package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.util.Arrays;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The ModelTransformationTag SHALL have 16 values representing the terms of the 4 by 4 transformation matrix.
 *
 * <p>The terms SHALL be in row-major order.
 */
public final class ModelTransformation {

    public static final String NAME = "MODEL_TRANSFORMATION";

    public static final short ID = (short) 0x85D8;

    public static double[][] getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<double[][]> getOptional(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Double d -> Optional.of(createTransformationMatrix(d.values()));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                 Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }

    static double[][] createTransformationMatrix(double[] doubles) {
        checkArgument(16 == doubles.length, "Should be exactly 16 elements, got %s", doubles.length);
        double[][] matrix = new double[4][4];
        for (int i = 0; i < 4; i++) {
            int offset = i * 4;
            matrix[i] = Arrays.copyOfRange(doubles, offset, offset + 4);
        }
        return matrix;
    }
}
