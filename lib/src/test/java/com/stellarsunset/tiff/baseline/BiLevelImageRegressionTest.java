package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class BiLevelImageRegressionTest {

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
                    () -> assertEquals(13, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{1}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(2, compression, "Compression"),
                    () -> assertEquals(0, photometricInterpretation, "Photometric Interpretation")
            );

//            TODO - Huffman Decompression
//            Image image = file.image(0);
//
//            if (unwrap(image) instanceof BiLevelImage b) {
//                assertAll(
//                        "Check Image(0) contents.",
//                        () -> assertEquals(280, b.dimensions().imageLength(), "Image Length (280)"),
//                        () -> assertEquals(272, b.dimensions().imageWidth(), "Image Width (272)"),
//                        () -> assertEquals(30, b.stripInfo().rowsPerStrip(), "Rows Per Strip")
//                );
//
//                assertAll(
//                        "Check Image(0) pixels.",
//                        () -> assertEquals(1, b.valueAt(0, 0).value()),
//                        () -> assertEquals(1, b.valueAt(0, 0).value()),
//                        () -> assertEquals(1, b.valueAt(0, 0).value()),
//                        () -> assertEquals(1, b.valueAt(0, 0).value())
//                );
//            } else {
//                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
//            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private Image unwrap(Image image) {
        return image instanceof Image.Lazy l ? l.delegate() : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
