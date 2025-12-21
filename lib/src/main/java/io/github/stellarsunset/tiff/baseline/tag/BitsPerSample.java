package io.github.stellarsunset.tiff.baseline.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Number of bits per component.
 *
 * <p>N = SamplesPerPixel. Type = {@link Entry.Short}.
 *
 * <p>Note that this field allows a different number of bits per component for each component corresponding to a
 * pixel. For example, RGB color data could use a different number of bits per component for each of the three
 * color planes.
 *
 * <p>Most RGB files will have the same number of BitsPerSample for each component. Even in this case, the writer
 * must write all three values.
 */
public final class BitsPerSample implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x102, "BITS_PER_SAMPLE");

    public static int[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<int[]> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUShortArray(TAG, ifd).or(() -> {
            OptionalInt samplesPerPixel = SamplesPerPixel.getIfPresent(ifd);
            return samplesPerPixel.isPresent()
                    ? Optional.of(createDefault(samplesPerPixel.getAsInt()))
                    : Optional.empty();
        });
    }

    static int[] createDefault(int samplesPerPixel) {
        int[] array = new int[samplesPerPixel];
        java.util.Arrays.fill(array, 1);
        return array;
    }
}
