package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.*;
import com.stellarsunset.tiff.extension.ShortImage.ShortNImage;
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

class LzwPredictorRegressionTest {

    private static final File FILE = tiffFile("extension/predictor.tif");

    @Test
    @Disabled("Differencing predictor not supported for non-8-bit images")
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
            int componentsPerPixel = SamplesPerPixel.getRequired(ifd);

            assertAll(
                    "Check IFD(0) contents.",
                    () -> assertEquals(22, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(5, compression, "Compression"),
                    () -> assertEquals(1, photometricInterpretation, "Photometric Interpretation"),
                    () -> assertEquals(1, planarConfiguration, "Planar Configuration"),
                    () -> assertEquals(15, componentsPerPixel, "Samples/Components Per Pixel")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof ShortNImage r) {

                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), r.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(448, r.dimensions().length(), "Image Length (448)"),
                        () -> assertEquals(rasters.getWidth(), r.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(539, r.dimensions().width(), "Image Width (539)"),
                        () -> assertEquals(1, StripInfo.getRequired(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertAll(
                        "Check Image(0) pixels.",
                        () -> comparePixelValues(r, rasters, 0, 0),
                        () -> comparePixelValues(r, rasters, 20, 20),
                        () -> comparePixelValues(r, rasters, 50, 110),
                        () -> comparePixelValues(r, rasters, 35, 80)
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void comparePixelValues(ShortNImage image, Rasters rasters, int row, int column) {

        Number[] rPixel = rasters.getPixel(column, row);
        assertEquals(15, rPixel.length, "Should return a single number for the BiLevel image pixel value.");

        Pixel.ShortN sPixel = image.valueAt(row, column);

        assertArrayEquals(toIntArray(rPixel), Arrays.toUnsignedIntArray(sPixel.values()),
                String.format("Should contain identical values at the respective pixel (%d, %d).", row, column));
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

    private int[] toIntArray(Number[] numbers) {
        int[] array = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            array[i] = (Integer) numbers[i];
        }
        return array;
    }
}
