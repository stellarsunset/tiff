package com.stellarsunset.tiff;

import com.stellarsunset.tiff.image.Image;

import java.io.File;

/**
 * Regression test against the NGA TIFF parser implementation.
 */
class TiffReaderRegressionTest {

    private static final File FILE = tiffFile("bilevel.tif");

    private Image unwrap(Image image) {
        return switch (image) {
            case Image.Lazy l -> l.delegate();
            default -> image;
        };
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
