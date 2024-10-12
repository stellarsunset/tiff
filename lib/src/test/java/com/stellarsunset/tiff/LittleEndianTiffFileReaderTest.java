package com.stellarsunset.tiff;

import com.stellarsunset.tiff.image.BiLevelImage;
import com.stellarsunset.tiff.image.Image;
import com.stellarsunset.tiff.tag.BitsPerSample;
import com.stellarsunset.tiff.tag.Compression;
import com.stellarsunset.tiff.tag.PhotometricInterpretation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-level test for reading an entire Little-Endian TIFF file.
 */
class LittleEndianTiffFileReaderTest {

    private static final File FILE = tiffFile("bilevel.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.read(FileChannel.open(FILE.toPath()))) {

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

            assertAll(
                    "Check IFD(0) contents.",
                    () -> assertEquals(14, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(32773, compression, "Compression"),
                    () -> assertEquals(1, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);

            if (unwrap(image) instanceof BiLevelImage b) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(280, b.dimensions().imageLength(), "Image Length"),
                        () -> assertEquals(272, b.dimensions().imageWidth(), "Image Width"),
                        () -> assertEquals(30, b.stripInfo().rowsPerStrip(), "Rows Per Strip")
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
