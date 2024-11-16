package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.RgbImage;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import com.stellarsunset.tiff.extension.tag.PlanarConfiguration;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

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

            int compression = Compression.getRequired(ifd);
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
        return image instanceof Image.Lazy l ? unwrap(l.delegate()) : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
