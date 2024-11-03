package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.PaletteColorImage;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.ColorMap;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import mil.nga.tiff.compression.LZWCompression;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LzwTest {

    private static final LZWCompression REFERENCE = new LZWCompression();

    private static final Lzw DECODER = new Lzw();

    @Test
    void testBitsInStream_TrimBitsLeftOf() {
        assertAll(
                () -> assertEquals(0b00011000, Lzw.BitsInStream.trimBitsLeftOf((byte) 0b00011000, 0)),
                () -> assertEquals(0b00001000, Lzw.BitsInStream.trimBitsLeftOf((byte) 0b00011000, 4)),
                () -> assertEquals(0b00000010, Lzw.BitsInStream.trimBitsLeftOf((byte) 0b00011010, 6))
        );
    }

    @Test
    void testBitsInStream_Simple() {

        Lzw.BitsInStream bis = new Lzw.BitsInStream(
                new byte[]{0b00000001, 0b00000010, (byte) 0b10000000, (byte) 0b11000000}
        );

        assertAll(
                () -> assertEquals((short) 0b000000010000, bis.readBitsAsShort(12),
                        "First 12 bits, should read fine from 0 bit offset"),
                () -> assertEquals((short) 0b001010000000, bis.readBitsAsShort(12),
                        "Second 12 bits, should read fine with initial bit offset")
        );

        bis.bitOffset(6);
        assertEquals((short) 0b010000001010, bis.readBitsAsShort(12),
                "First 12 bits (offset 6), should read third byte to span request as needed.");
    }

    /**
     * These are the initial bytes from a real LZW-compressed TIFF file.
     */
    @Test
    void testBitsInStream_Real() {

        // 10000000, 00000111, 00100000, 01010000, 00111000, 00100100, ... EOI Code (100000001)
        // first 9 entries make 8 complete codes, then we split the EOI code across two binary numbers
        byte[] bytes = new byte[]{-128, 7, 32, 80, 56, 36, 22, 13, 6, (byte) 0b10000000, (byte) 0b10000000};

        Lzw.BitsInStream bis = new Lzw.BitsInStream(bytes);

        byte[] expected = REFERENCE.decode(bytes, ByteOrder.LITTLE_ENDIAN);

        assertAll(
                () -> assertEquals((short) 256, bis.readBitsAsShort(9), "First 9 bits"),
                () -> assertEquals((short) 28, bis.readBitsAsShort(9), "Second 9 bits"),
                () -> assertEquals((short) 258, bis.readBitsAsShort(9), "Third 9 bits"),
                () -> assertEquals((short) 259, bis.readBitsAsShort(9), "Fourth 9 bits"),
                () -> assertEquals((short) 260, bis.readBitsAsShort(9), "Fifth 9 bits")
        );

        byte[] actual = DECODER.decompress(bytes, BytesAdapter.of(ByteOrder.LITTLE_ENDIAN));
        assertArrayEquals(expected, actual, "Should produce identical decompressed byte sequence.");
    }

    @Test
    void testCodeTable() {

        Lzw.CodeTable table = new Lzw.CodeTable();

        assertAll(
                "Check initialization",
                () -> assertTrue(table.containsCode((short) 1), "Should contain value <= 256"),
                () -> assertFalse(table.containsCode((short) 500), "Should not contain value > 256"),
                () -> assertArrayEquals(new byte[]{(byte) 1}, table.bytesForCode((short) 1),
                        "Table should contain byte value 1 for code 1"),
                () -> assertEquals(9, table.codeBits(), "Initial code bits should be 9")
        );

        for (int i = 0; i < 512; i++) {
            table.addNextCode(new byte[0]);
        }

        assertEquals(10, table.codeBits(), "Code bits should be 10 after 512 iterations.");
    }

    @Test
    void testAppendByte() {
        assertArrayEquals(new byte[]{1, 2, 3}, Lzw.appendByte(new byte[]{1, 2}, (byte) 3));
    }

    private static final File FILE = tiffFile("compress/lzw.tif");

    /**
     * Test decompression of a LZW-compressed Palette-Color TIFF image.
     */
    @Test
    void testFile() {
        try (TiffFile file = TiffFileReader.baseline().read(FileChannel.open(FILE.toPath()))) {

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
                    () -> assertEquals(22, ifd.entryCount(), "IFD Entry Count"),
                    () -> assertArrayEquals(new int[]{8}, bitsPerSample, "Bits Per Sample"),
                    () -> assertEquals(5, compression, "Compression"),
                    () -> assertEquals(3, photometricInterpretation, "Photometric Interpretation")
            );

            Image image = file.image(0);

            FileDirectory fileDirectory = TiffReader.readTiff(FILE).getFileDirectory();

            List<Integer> colorMap = fileDirectory.getColorMap();
            Rasters rasters = fileDirectory.readRasters();

            if (unwrap(image) instanceof PaletteColorImage p) {
                assertAll(
                        "Check Image(0) contents.",
                        () -> assertEquals(72, p.dimensions().imageLength(), "Image Length (280)"),
                        () -> assertEquals(128, p.dimensions().imageWidth(), "Image Width (272)"),
                        () -> assertEquals(72, StripInfo.from(ifd).rowsPerStrip(), "Rows Per Strip")
                );

                assertEquals(colorMap, flattenColorMap(p.colorMap()), "Color Map");

                assertAll(
                        "Checking Image(0) pixels.",
                        () -> comparePixelValues(p, rasters, 0, 0),
                        () -> comparePixelValues(p, rasters, 55, 100),
                        () -> comparePixelValues(p, rasters, 32, 74)
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

        Pixel.PaletteColor iPixel = image.valueAt(row, column);
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
