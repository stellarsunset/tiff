package com.stellarsunset.terrain.tiff.image;

import com.google.common.base.Preconditions;
import com.stellarsunset.terrain.tiff.Ifd;
import com.stellarsunset.terrain.tiff.tag.RowsPerStrip;
import com.stellarsunset.terrain.tiff.tag.StripByteCounts;
import com.stellarsunset.terrain.tiff.tag.StripOffsets;

/**
 * Container class for the related baseline tags:
 * <ol>
 *     <li>{@link RowsPerStrip}</li>
 *     <li>{@link StripOffsets}</li>
 *     <li>{@link StripByteCounts}</li>
 * </ol>
 */
public record StripInfo(long rowsPerStrip, long[] stripOffsets, long[] stripByteCounts) {

    public static StripInfo from(Ifd ifd) {
        return new StripInfo(RowsPerStrip.getRequired(ifd), StripOffsets.getRequired(ifd), StripByteCounts.getRequired(ifd));
    }

    public Int asIntInfo() {

        Preconditions.checkArgument(rowsPerStrip < java.lang.Integer.MAX_VALUE,
                "RowsPerStrip should be less than Integer.MAX_VALUE, value was %s", rowsPerStrip);

        int[] intByteCounts = new int[stripByteCounts.length];
        for (int i = 0; i < stripByteCounts.length; i++) {

            long stripByteCount = stripByteCounts[i];

            Preconditions.checkArgument(stripByteCounts.length < java.lang.Integer.MAX_VALUE,
                    "StripByteCount should be less than Integer.MAX_VALUE, value was %s for strip %s", stripByteCount, i);

            intByteCounts[i] = (int) stripByteCount;
        }

        return new Int((int) rowsPerStrip, stripOffsets, intByteCounts);
    }

    /**
     * The {@code byte[]}s we want to read the strips into are integer-indexed as opposed to long indexed, while in theory
     * the values from the file are unsigned integer values (so they may be longer than the standard signed Java int).
     *
     * <p>This makes it annoying to work with the strips in Java, but 99% of the time the values we get from the file are
     * truly less than {@link java.lang.Integer#MAX_VALUE}. This class allows us to down-convert the real file values to
     * integer ones for ease-of-use but will throw exceptions if our expectations aren't met.
     */
    public record Int(int rowsPerStrip, long[] stripOffsets, int[] stripByteCounts) {

    }
}
