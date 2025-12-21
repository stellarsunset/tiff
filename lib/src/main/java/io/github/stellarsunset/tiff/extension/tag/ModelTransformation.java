package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Arrays;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The ModelTransformationTag SHALL have 16 values representing the terms of the 4 by 4 transformation matrix.
 *
 * <p>The terms SHALL be in row-major order.
 */
public final class ModelTransformation implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x85D8, "MODEL_TRANSFORMATION");

    public static double[][] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<double[][]> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalDoubleArray(TAG, ifd).map(ModelTransformation::createTransformationMatrix);
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
