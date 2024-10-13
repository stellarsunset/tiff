package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.TiffFile;
import com.stellarsunset.tiff.TiffFileReader;
import com.stellarsunset.tiff.TiffHeader;
import com.stellarsunset.tiff.tag.BitsPerSample;
import com.stellarsunset.tiff.tag.Compression;
import com.stellarsunset.tiff.tag.PhotometricInterpretation;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class GrayscaleImageRegressionTest {

    private static final File FILE = tiffFile("grayscale.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.BIG_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(1, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.getRequired(ifd);
            int photometricInterpretation = PhotometricInterpretation.getRequired(ifd);

            assertAll(
                    "Check IFD(0) contents.",
                    () -> assertEquals(14, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(32773, compression, "Compression"),
                    () -> assertEquals(1, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);
            Rasters rasters = readRasters();

            if (unwrap(image) instanceof BiLevelImage b) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), b.dimensions().imageLength(), "Image Length Matches"),
                        () -> assertEquals(256, b.dimensions().imageLength(), "Image Length (256)"),
                        () -> assertEquals(rasters.getWidth(), b.dimensions().imageWidth(), "Image Width Matches"),
                        () -> assertEquals(256, b.dimensions().imageWidth(), "Image Width (256)"),
                        () -> assertEquals(32, b.stripInfo().rowsPerStrip(), "Rows Per Strip")
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void comparePixelValues(BiLevelImage image, Rasters rasters, int row, int column) {

        Number[] rPixel = rasters.getPixel(column, row);
        assertEquals(1, rPixel.length, "Should return a single number for the BiLevel image pixel value.");

        PixelValue.BlackOrWhite iPixel = image.valueAt(row, column);
        assertEquals(Short.toUnsignedInt((Short) rPixel[0]), Byte.toUnsignedInt(iPixel.value()), String.format("Should contain identical values at the respective pixel (%d, %d).", row, column));
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
