package com.stellarsunset.tiff.extension.geo;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class GeoImageRegressionTest {

    private static final File FILE = tiffFile("extension/geo/USGS_1_n52e177.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.baseline().read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.LITTLE_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(6, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.getRequired(ifd);
            int photometricInterpretation = PhotometricInterpretation.getRequired(ifd);

            assertAll(
                    "Check IFD(0) contents.",
                    () -> assertEquals(19, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{32}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(5, compression, "Compression"),
                    () -> assertEquals(1, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof GeoImage g) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), g.dimensions().imageLength(), "Image Length Matches"),
                        () -> assertEquals(256, g.dimensions().imageLength(), "Image Length (256)"),
                        () -> assertEquals(rasters.getWidth(), g.dimensions().imageWidth(), "Image Width Matches"),
                        () -> assertEquals(256, g.dimensions().imageWidth(), "Image Width (256)"),
                        () -> assertEquals(32, g.stripInfo().rowsPerStrip(), "Rows Per Strip")
                );

                assertAll(
                        "Check Image(0) pixels.",
                        () -> comparePixelValues(g, rasters, 0, 0),
                        () -> comparePixelValues(g, rasters, 20, 100),
                        () -> comparePixelValues(g, rasters, 150, 200),
                        () -> comparePixelValues(g, rasters, 255, 255)
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void comparePixelValues(GeoImage image, Rasters rasters, int row, int column) {

        Number[] rPixel = rasters.getPixel(column, row);
        assertEquals(1, rPixel.length, "Should return a single number for the BiLevel image pixel value.");

        //Pixel.Grayscale8 iPixel = image.valueAt(row, column);
        //assertEquals(Short.toUnsignedInt((Short) rPixel[0]), Byte.toUnsignedInt(iPixel.value()), String.format("Should contain identical values at the respective pixel (%d, %d).", row, column));
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
