package com.stellarsunset.tiff.baseline;

import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;

import java.io.File;
import java.io.IOException;

final class NgaImageReader {

    private NgaImageReader() {
    }

    public static byte[][] loadByteImage(String subpath, int index) throws IOException {
        Rasters rasters = TiffReader.readTiff(resourceFile(subpath)).getFileDirectory().readRasters();

        int w = rasters.getWidth();
        int h = rasters.getHeight();

        int componentsPerPixel = rasters.getBitsPerSample().size();
        byte[][] bytes = new byte[rasters.getHeight()][rasters.getWidth() * componentsPerPixel];

        for (int r = 0; r < h; r++) {
            byte[] row = bytes[r];
            for (int c = 0; c < w; c++) {
                int offset = c * componentsPerPixel;
                Number[] numbers = rasters.getPixel(c, r);
                for (int comp = 0; comp < componentsPerPixel; comp++) {
                    row[offset + comp] = (byte) (short) numbers[comp];
                }
            }
        }

        return bytes;
    }

    private static File resourceFile(String subPath) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + subPath);
    }
}
