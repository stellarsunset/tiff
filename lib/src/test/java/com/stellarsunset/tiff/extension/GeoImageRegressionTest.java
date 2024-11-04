package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import com.stellarsunset.tiff.extension.geo.GeoKeyDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class GeoImageRegressionTest {

    private static final File FILE = tiffFile("extension/geo/USGS_1_n52e177.tif");

    @Test
    @Disabled("Not ready yet...")
    void test() {
        try (TiffFile file = TiffFileReader.withMaker(Image.Maker.lazy(FloatImage.maker())).read(FileChannel.open(FILE.toPath()))) {

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

            GeoKeyDirectory gkd = GeoKeyDirectory.getRequired(ifd);

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof FloatImage f) {

                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), f.dimensions().imageLength(), "Image Length Matches"),
                        () -> assertEquals(3612, f.dimensions().imageLength(), "Image Length (256)"),
                        () -> assertEquals(rasters.getWidth(), f.dimensions().imageWidth(), "Image Width Matches"),
                        () -> assertEquals(3612, f.dimensions().imageWidth(), "Image Width (256)"),
                        () -> assertEquals(32, StripInfo.from(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertAll(
                        "Check Image(0) pixels.",
                        () -> comparePixelValues(f, rasters, 0, 0),
                        () -> comparePixelValues(f, rasters, 20, 100),
                        () -> comparePixelValues(f, rasters, 150, 200),
                        () -> comparePixelValues(f, rasters, 255, 255)
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void comparePixelValues(FloatImage image, Rasters rasters, int row, int column) {

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
