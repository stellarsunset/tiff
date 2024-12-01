package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.RasterHelpers;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import com.stellarsunset.tiff.extension.FloatImage.Float1Image;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class FloatImageRegressionTest {

    /**
     * This is USGS elevation data, which is the only reason I wrote this library...
     */
    private static final File FILE = tiffFile("extension/float-predictor-tiled.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.withMaker(DataImage.maker()).read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.LITTLE_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(6, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.get(ifd);
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

            if (unwrap(image) instanceof Float1Image f) {

                TileInfo tileInfo = TileInfo.getRequired(ifd);

                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), f.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(1812, f.dimensions().length(), "Image Length (1812)"),
                        () -> assertEquals(rasters.getWidth(), f.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(1812, f.dimensions().width(), "Image Width (1812)"),
                        () -> assertEquals(512, tileInfo.length(), "Tile Length"),
                        () -> assertEquals(512, tileInfo.width(), "Tile Width")
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
        return image instanceof Image.Lazy l ? l.delegate() : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
