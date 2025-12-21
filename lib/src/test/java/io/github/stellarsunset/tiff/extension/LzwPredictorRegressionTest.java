package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.*;
import io.github.stellarsunset.tiff.baseline.RasterHelpers;
import io.github.stellarsunset.tiff.baseline.StripInfo;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.Compression;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import io.github.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import io.github.stellarsunset.tiff.extension.ShortImage.ShortNImage;
import io.github.stellarsunset.tiff.extension.tag.PlanarConfiguration;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class LzwPredictorRegressionTest {

    private static final File FILE = tiffFile("extension/predictor.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.withMaker(DataImage.maker()).read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.LITTLE_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(1, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.get(ifd);

            int compression = Compression.get(ifd);
            int photometricInterpretation = PhotometricInterpretation.get(ifd);
            int planarConfiguration = PlanarConfiguration.get(ifd);
            int componentsPerPixel = SamplesPerPixel.get(ifd);

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

                assertArrayEquals(RasterHelpers.toShortRaster(rasters), r.data(), "Raster Data");
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
