package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.*;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.ColorMap;
import io.github.stellarsunset.tiff.baseline.tag.Compression;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
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

    private static final File FILE = tiffFile("baseline/palette.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.baseline().read(FileChannel.open(FILE.toPath()))) {

            TiffHeader header = file.header();

            assertAll(
                    "Check the top-level file contents.",
                    () -> assertEquals(ByteOrder.LITTLE_ENDIAN, header.order(), "ByteOrder"),
                    () -> assertEquals(1, file.numberOfImages(), "Number of Images")
            );

            Ifd ifd = file.ifd(0);

            int[] bitsPerSample = BitsPerSample.getRequired(ifd);

            int compression = Compression.get(ifd);
            int photometricInterpretation = PhotometricInterpretation.get(ifd);

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
                        () -> assertEquals(rasters.getHeight(), p.dimensions().length(), "Image Length Matches"),
                        () -> assertEquals(756, p.dimensions().length(), "Image Length (756)"),
                        () -> assertEquals(rasters.getWidth(), p.dimensions().width(), "Image Width Matches"),
                        () -> assertEquals(1008, p.dimensions().width(), "Image Width (1008)"),
                        () -> assertEquals(756, StripInfo.getRequired(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertEquals(colorMap, flattenColorMap(p.colorMap()), "Color Map");
                assertArrayEquals(RasterHelpers.toByteRaster(rasters), p.data(), "Raster Data");
            } else {
                fail("Image not of the correct type, image type was: " + unwrap(image).getClass().getSimpleName());
            }
        } catch (Exception e) {
            fail(e);
        }
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
