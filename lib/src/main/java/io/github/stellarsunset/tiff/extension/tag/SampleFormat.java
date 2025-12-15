package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

/**
 * This field specifies how to interpret each data sample in a pixel. Possible values are:
 * <ol>
 *     <li>1 = unsigned integer data</li>
 *     <li>2 = two’s complement signed integer data</li>
 *     <li>3 = IEEE floating point data [IEEE]</li>
 *     <li>4 = undefined data format</li>
 * </ol>
 *
 * <p>Note that the SampleFormat field does not specify the size of data samples; this is still done by the BitsPerSample
 * field.
 *
 * <p>A field value of “undefined” is a statement by the writer that it did not know how to interpret the data samples;
 * for example, if it were copying an existing image. A reader would typically treat an image with “undefined” data as
 * if the field were not present (i.e. as unsigned integer data).
 *
 * <p>Default is 1, unsigned integer data.
 *
 * <p>This returning an array means that different components of the same pixel may be different types... which is a bit
 * wild, we don't support this.
 */
public final class SampleFormat {

    public static final String NAME = "SAMPLE_FORMAT";

    public static final short ID = 0x153;

    public static int[] get(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Arrays.toUnsignedIntArray(s.values());
            case Entry.NotFound _ -> createDefault(ifd);
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }

    static int[] createDefault(Ifd ifd) {
        int samplesPerPixel = SamplesPerPixel.get(ifd);
        int[] array = new int[samplesPerPixel];
        java.util.Arrays.fill(array, 1);
        return array;
    }
}
