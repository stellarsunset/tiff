package com.stellarsunset.tiff.baseline.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;

import java.util.Optional;

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
public final class BitsPerSample {

    public static final String NAME = "BITS_PER_SAMPLE";

    public static final short ID = 0x102;

    public static int[] getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    // TODO - respect SamplesPerPixel in the default?
    public static Optional<int[]> getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Optional.of(Arrays.toUnsignedIntArray(s.values()));
            case Entry.NotFound _ -> Optional.of(new int[]{1});
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
