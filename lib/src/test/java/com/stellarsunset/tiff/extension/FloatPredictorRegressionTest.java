package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.RasterHelpers;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import com.stellarsunset.tiff.extension.FloatImage.Float3Image;
import com.stellarsunset.tiff.extension.tag.PlanarConfiguration;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class FloatPredictorRegressionTest {

    private static final File FILE = tiffFile("extension/float-predictor-rgb.tif");

    @Test
    @Disabled("Differencing predictor not supported for floating point images")
    void test() {
        try (TiffFile file = TiffFileReader.withMaker(DataImage.maker()).read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.LITTLE_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(1, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.get(ifd);
            int photometricInterpretation = PhotometricInterpretation.getRequired(ifd);
            int planarConfiguration = PlanarConfiguration.getRequired(ifd);

            assertAll(
                    "Check IFD(0) contents.",
                    () -> assertEquals(25, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{32, 32, 32}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(5, compression, "Compression"),
                    () -> assertEquals(2, photometricInterpretation, "Photometric Interpretation"),
                    () -> assertEquals(1, planarConfiguration, "Planar Configuration")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof Float3Image f) {

                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), f.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(72, f.dimensions().length(), "Image Length (72)"),
                        () -> assertEquals(rasters.getWidth(), f.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(128, f.dimensions().width(), "Image Width (128)"),
                        () -> assertEquals(72, StripInfo.getRequired(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertArrayEquals(RasterHelpers.toFloatRaster(rasters), f.data(), "Raster Data");
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
        return image instanceof Image.Lazy l ? unwrap(l.delegate()) : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
