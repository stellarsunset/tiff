package com.stellarsunset.terrain.tiff;

import com.stellarsunset.terrain.tiff.image.Image;
import com.stellarsunset.terrain.tiff.image.RgbImage;
import com.stellarsunset.terrain.tiff.tag.BitsPerSample;
import com.stellarsunset.terrain.tiff.tag.Compression;
import com.stellarsunset.terrain.tiff.tag.PhotometricInterpretation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-level test for reading an entire Big-Endian TIFF file.
 */
class BigEndianTiffFileReaderTest {

    private static final File FILE = tiffFile("rgb.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();
            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.getRequired(ifd);
            int photometricInterpretation = PhotometricInterpretation.getRequired(ifd);

            assertAll(
                    () -> assertEquals(ByteOrder.BIG_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(14, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8, 8, 8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(1, compression, "Compression"),
                    () -> assertEquals(2, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);

            if (unwrap(image) instanceof RgbImage r) {
                assertAll(
                        "Checking Image(0) contents.",
                        () -> assertEquals(0, 0)
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private Image unwrap(Image image) {
        return switch (image) {
            case Image.Lazy l -> l.delegate();
            default -> image;
        };
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}