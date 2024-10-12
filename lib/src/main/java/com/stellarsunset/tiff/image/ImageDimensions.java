package com.stellarsunset.tiff.image;

import com.google.common.base.Preconditions;
import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.tag.ImageLength;
import com.stellarsunset.tiff.tag.ImageWidth;

/**
 * Container class for the {@link ImageLength} and {@link ImageWidth} tags.
 *
 * <p>Parameter types are Java longs to contain the unsigned short/int values.
 */
public record ImageDimensions(long imageLength, long imageWidth) {

    public static ImageDimensions from(Ifd ifd) {
        return new ImageDimensions(ImageLength.getRequired(ifd), ImageWidth.getRequired(ifd));
    }

    public Int asIntInfo() {

        Preconditions.checkArgument(imageLength < java.lang.Integer.MAX_VALUE,
                "ImageLength should be less than Integer.MAX_VALUE, value was %s", imageLength);

        Preconditions.checkArgument(imageWidth < java.lang.Integer.MAX_VALUE,
                "ImageWidth should be less than Integer.MAX_VALUE, value was %s", imageWidth);

        return new Int((int) imageLength, (int) imageWidth);
    }

    /**
     * See {@link StripInfo.Int} for reasoning behind this truncation.
     */
    public record Int(int imageLength, int imageWidth) {

    }
}
