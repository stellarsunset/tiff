package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.*;
import io.github.stellarsunset.tiff.baseline.RasterHelpers;
import io.github.stellarsunset.tiff.baseline.RgbImage;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.Compression;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import io.github.stellarsunset.tiff.extension.tag.DifferencingPredictor;
import io.github.stellarsunset.tiff.extension.tag.PlanarConfiguration;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note - this also has a {@link DifferencingPredictor} applied.
 */
class TiledImageRegressionTest {

    private static final File FILE = tiffFile("extension/tiled-rgb.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.withMaker(Image.Maker.baseline()).read(FileChannel.open(FILE.toPath()))) {

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
                    () -> assertEquals(23, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8, 8, 8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(5, compression, "Compression"),
                    () -> assertEquals(2, photometricInterpretation, "Photometric Interpretation"),
                    () -> assertEquals(1, planarConfiguration, "Planar Configuration")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof RgbImage r) {

                TileInfo tileInfo = TileInfo.getRequired(ifd);

                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), r.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(72, r.dimensions().length(), "Image Length (72)"),
                        () -> assertEquals(rasters.getWidth(), r.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(128, r.dimensions().width(), "Image Width (128)"),
                        () -> assertEquals(32, tileInfo.length(), "Tile Length (32)"),
                        () -> assertEquals(32, tileInfo.width(), "Tile Width (32)")
                );

                assertArrayEquals(RasterHelpers.toByteRaster(rasters), r.data(), "Raster Data");
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
