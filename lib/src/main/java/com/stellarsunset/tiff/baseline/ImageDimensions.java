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
public record ImageDimensions(long length, long width) {

    public static ImageDimensions get(Ifd ifd) {
        return new ImageDimensions(ImageLength.getRequired(ifd), ImageWidth.getRequired(ifd));
    }

    public Int asIntInfo() {

        checkArgument(length < java.lang.Integer.MAX_VALUE,
                "ImageLength should be less than Integer.MAX_VALUE, value was %s", length);

        checkArgument(width < java.lang.Integer.MAX_VALUE,
                "ImageWidth should be less than Integer.MAX_VALUE, value was %s", width);

        return new Int((int) length, (int) width);
    }

    /**
     * Shorthand to check the bounds materialized image data against the expected byte image dimensions.
     *
     * @param data          the materialized image bytes
     * @param bytesPerPixel the number of bytes per pixel of the image, e.g. 1 for BiLevel, 3 for RGB
     */
    public void checkBounds(byte[][] data, int bytesPerPixel) {
        checkArgument(data.length == length(),
                "Expected %s rows, found %s", length(), data.length);

        checkArgument(data[0].length == width() * bytesPerPixel,
                "Expected %s * %s columns, found %s", width(), bytesPerPixel, data[0].length);
    }

    /**
     * Shorthand to check the bounds materialized image data against the expected short image dimensions.
     *
     * @param data           the materialized image bytes
     * @param shortsPerPixel the number of shorts per pixel of the image
     */
    public void checkBounds(short[][] data, int shortsPerPixel) {
        checkArgument(data.length == length(),
                "Expected %s rows, found %s", length(), data.length);

        checkArgument(data[0].length == width() * shortsPerPixel,
                "Expected %s * %s columns, found %s", width(), shortsPerPixel, data[0].length);
    }

    /**
     * Shorthand to check the bounds materialized image data against the expected integer image dimensions.
     *
     * @param data         the materialized image bytes
     * @param intsPerPixel the number of integers per pixel of the image
     */
    public void checkBounds(int[][] data, int intsPerPixel) {
        checkArgument(data.length == length(),
                "Expected %s rows, found %s", length(), data.length);

        checkArgument(data[0].length == width() * intsPerPixel,
                "Expected %s * %s columns, found %s", width(), intsPerPixel, data[0].length);
    }

    /**
     * Shorthand to check the bounds materialized image data against the expected float image dimensions.
     *
     * @param data           the materialized image bytes
     * @param floatsPerPixel the number of floats per pixel of the image
     */
    public void checkBounds(float[][] data, int floatsPerPixel) {
        checkArgument(data.length == length(),
                "Expected %s rows, found %s", length(), data.length);

        checkArgument(data[0].length == width() * floatsPerPixel,
                "Expected %s * %s columns, found %s", width(), floatsPerPixel, data[0].length);
    }

    /**
     * See {@link StripInfo.Int} for reasoning behind this truncation.
     */
    public record Int(int length, int width) {

    }
}
