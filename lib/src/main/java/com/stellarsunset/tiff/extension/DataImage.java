package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.baseline.BaselineImage;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.extension.tag.SampleFormat;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toSet;

/**
 * Extension image type representing an image as a carrier of "data" in its raster values.
 *
 * <p>This is a general-purpose way to get back sanely typed image handles with the appropriate primitive values in their
 * raster arrays.
 *
 * <p>The {@link DataImage.Maker} handle can be used to parse the image contents of all TIFF 6.0 {@link BaselineImage}
 * images.
 */
public sealed interface DataImage extends ExtensionImage permits ByteImage, ShortImage, IntImage, FloatImage {

    static Image.Maker maker() {
        return new Maker();
    }

    record Maker(Image.Maker bytes, Image.Maker shorts, Image.Maker ints, Image.Maker floats) implements Image.Maker {

        public Maker() {
            this(
                    ByteImage.maker(),
                    ShortImage.maker(),
                    IntImage.maker(),
                    FloatImage.maker()
            );
        }

        @Override
        public Image makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int[] samples = BitsPerSample.getRequired(ifd);
            checkAllEqual(samples, "Should be exactly one component pixel width in the array, instead got %s");

            int[] formats = SampleFormat.get(ifd);
            checkAllEqual(formats, "Should be exactly one format for component pixels, got %s");

            int bitsPerSample = samples[0];
            int format = formats[0];

            Image.Maker maker = switch (bitsPerSample) {
                case 8 -> bytes;
                case 16 -> shorts;
                case 32 -> format == 3 ? floats : ints;
                default -> throw new IllegalArgumentException(
                        String.format("Unable to handle odd bits-per-sample count %s, should be 8, 16, or 32.", bitsPerSample)
                );
            };

            return maker.makeImage(channel, order, ifd);
        }

        static void checkAllEqual(int[] samples, String messageFormat) {

            Set<String> sizes = IntStream.range(0, samples.length)
                    .boxed()
                    .map(i -> Integer.toString(samples[i]))
                    .collect(toSet());

            checkArgument(sizes.size() == 1,
                    messageFormat, String.join(",", sizes));
        }
    }
}
