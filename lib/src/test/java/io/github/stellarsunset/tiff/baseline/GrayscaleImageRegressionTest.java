package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.*;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.Compression;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class GrayscaleImageRegressionTest {

    private static final File FILE = tiffFile("baseline/grayscale.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.baseline().read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.BIG_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(1, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.get(ifd);

            int compression = Compression.get(ifd);
            int photometricInterpretation = PhotometricInterpretation.get(ifd);

            assertAll(
                    "Check IFD(0) contents.",
                    () -> assertEquals(14, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(32773, compression, "Compression"),
                    () -> assertEquals(1, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof GrayscaleImage.Grayscale8Image g) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), g.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(256, g.dimensions().length(), "Image Length (256)"),
                        () -> assertEquals(rasters.getWidth(), g.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(256, g.dimensions().width(), "Image Width (256)"),
                        () -> assertEquals(32, StripInfo.getRequired(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertArrayEquals(RasterHelpers.toByteRaster(rasters), g.data(), "Raster Data");
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private Rasters readRasters() throws IOException {
        return TiffReader.readTiff(FILE).getFileDirectory().readRasters();
    }

    private Image unwrap(Image image) {
        return image instanceof Image.Lazy l ? l.delegate() : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
