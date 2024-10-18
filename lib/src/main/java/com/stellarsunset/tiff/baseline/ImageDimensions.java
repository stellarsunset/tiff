package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.baseline.tag.ImageLength;
import com.stellarsunset.tiff.baseline.tag.ImageWidth;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Container class for the {@link ImageLength} and {@link ImageWidth} tags.
 *
 * <p>The width of the image is the number of columns in the image, the length is the number of rows.
 *
 * <p>Parameter types are Java longs to contain the unsigned short/int values.
 */
public record ImageDimensions(long imageLength, long imageWidth) {

    public static ImageDimensions from(Ifd ifd) {
        return new ImageDimensions(ImageLength.getRequired(ifd), ImageWidth.getRequired(ifd));
    }

    public Int asIntInfo() {

        checkArgument(imageLength < java.lang.Integer.MAX_VALUE,
                "ImageLength should be less than Integer.MAX_VALUE, value was %s", imageLength);

        checkArgument(imageWidth < java.lang.Integer.MAX_VALUE,
                "ImageWidth should be less than Integer.MAX_VALUE, value was %s", imageWidth);

        return new Int((int) imageLength, (int) imageWidth);
    }

    /**
     * Shorthand to check the bounds materialized image data against the expected image dimensions.
     *
     * @param data          the materialized image bytes
     * @param bytesPerPixel the number of bytes per pixel of the image, e.g. 1 for BiLevel, 3 for RGB
     */
    public void checkBounds(byte[][] data, int bytesPerPixel) {
        checkArgument(data.length == imageLength(),
                "Expected %s rows, found %s", imageLength(), data.length);

        checkArgument(data[0].length == imageWidth() * bytesPerPixel,
                "Expected %s * %s columns, found %s", imageWidth(), bytesPerPixel, data[0].length);
    }

    /**
     * See {@link StripInfo.Int} for reasoning behind this truncation.
     */
    public record Int(int imageLength, int imageWidth) {

    }
}
