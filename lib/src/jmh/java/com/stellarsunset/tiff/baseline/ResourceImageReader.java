package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.TiffFile;
import com.stellarsunset.tiff.TiffFileReader;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

final class ResourceImageReader {

    private ResourceImageReader() {
    }

    /**
     * Materialize the image data from a file on the local filesystem.
     *
     * @param subPath the subpath to load the image data from
     * @param index   the index of the image in the TIFF file to load
     */
    public static Image loadImage(String subPath, int index) throws IOException {
        try (TiffFile file = TiffFileReader.baseline().read(FileChannel.open(resourceFile(subPath).toPath()))) {
            Image image = file.image(index);
            if (image instanceof Image.Lazy l) {
                return l.delegate();
            }
            return image;
        }
    }

    private static File resourceFile(String subPath) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + subPath);
    }
}
