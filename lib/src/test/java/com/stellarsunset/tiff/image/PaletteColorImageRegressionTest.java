package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.TiffFile;
import com.stellarsunset.tiff.TiffFileReader;
import com.stellarsunset.tiff.TiffHeader;
import com.stellarsunset.tiff.tag.BitsPerSample;
import com.stellarsunset.tiff.tag.ColorMap;
import com.stellarsunset.tiff.tag.Compression;
import com.stellarsunset.tiff.tag.PhotometricInterpretation;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PaletteColorImageRegressionTest {

    private static final File FILE = tiffFile("palette.tif");

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
                    () -> assertEquals(21, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(1, compression, "Compression"),
                    () -> assertEquals(3, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);

            FileDirectory fileDirectory = TiffReader.readTiff(FILE).getFileDirectory();

            List<Integer> colorMap = fileDirectory.getColorMap();
            Rasters rasters = fileDirectory.readRasters();

            if (unwrap(image) instanceof PaletteColorImage p) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(rasters.getHeight(), p.dimensions().imageLength(), "Image Length Matches"),
                        () -> assertEquals(756, p.dimensions().imageLength(), "Image Length (756)"),
                        () -> assertEquals(rasters.getWidth(), p.dimensions().imageWidth(), "Image Width Matches"),
                        () -> assertEquals(1008, p.dimensions().imageWidth(), "Image Width (1008)"),
                        () -> assertEquals(756, p.stripInfo().rowsPerStrip(), "Rows Per Strip")
                );

                assertEquals(colorMap, flattenColorMap(p.colorMap()), "Color Map");

                assertAll(
                        "Checking Image(0) pixels.",
                        () -> comparePixelValues(p, rasters, 0, 0),
                        () -> comparePixelValues(p, rasters, 250, 321),
                        () -> comparePixelValues(p, rasters, 442, 454)
                );
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private void comparePixelValues(PaletteColorImage image, Rasters rasters, int row, int column) {

        Number[] rPixel = rasters.getPixel(column, row);
        assertEquals(1, rPixel.length, "Should return a single number for the Palette-Color image pixel value.");

        PixelValue.PaletteColor iPixel = image.valueAt(row, column);
        assertEquals(Short.toUnsignedInt((Short) rPixel[0]), iPixel.unsignedIndex(), String.format("Should contain identical ColorMap index at the respective pixel (%d, %d).", row, column));
    }

    private List<Integer> flattenColorMap(ColorMap colorMap) {
        short[] flattened = colorMap.flatten();
        return IntStream.range(0, flattened.length).mapToObj(i -> Short.toUnsignedInt(flattened[i])).toList();
    }

    private Image unwrap(Image image) {
        return image instanceof Image.Lazy l ? l.delegate() : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
