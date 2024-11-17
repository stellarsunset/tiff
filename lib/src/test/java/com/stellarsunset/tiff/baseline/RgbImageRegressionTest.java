package com.stellarsunset.tiff.baseline;

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

class RgbImageRegressionTest {

    private static final File FILE = tiffFile("baseline/rgb.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.baseline().read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();
            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.get(ifd);
            int photometricInterpretation = PhotometricInterpretation.getRequired(ifd);

            assertAll(
                    () -> assertEquals(ByteOrder.BIG_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(14, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8, 8, 8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(1, compression, "Compression"),
                    () -> assertEquals(2, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof RgbImage r) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), r.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(443, r.dimensions().length(), "Image Length (443)"),
                        () -> assertEquals(rasters.getWidth(), r.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(455, r.dimensions().width(), "Image Width (455)"),
                        () -> assertEquals(443, StripInfo.getRequired(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertAll(
                        "Checking Image(0) pixels.",
                        () -> comparePixelValues(r, rasters, 0, 0),
                        () -> comparePixelValues(r, rasters, 250, 321),
                        () -> comparePixelValues(r, rasters, 442, 454)
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void comparePixelValues(RgbImage image, Rasters rasters, int row, int column) {

        Number[] rPixel = rasters.getPixel(column, row);
        assertEquals(3, rPixel.length, "Should return a single number for the BiLevel image pixel value.");

        Pixel.Rgb iPixel = image.valueAt(row, column);
        assertAll(
                () -> assertEquals(Short.toUnsignedInt((Short) rPixel[0]), Byte.toUnsignedInt(iPixel.r()), String.format("Should contain identical Red values at the respective pixel (%d, %d).", row, column)),
                () -> assertEquals(Short.toUnsignedInt((Short) rPixel[1]), Byte.toUnsignedInt(iPixel.g()), String.format("Should contain identical Green values at the respective pixel (%d, %d).", row, column)),
                () -> assertEquals(Short.toUnsignedInt((Short) rPixel[2]), Byte.toUnsignedInt(iPixel.b()), String.format("Should contain identical Blue values at the respective pixel (%d, %d).", row, column))
        );
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
