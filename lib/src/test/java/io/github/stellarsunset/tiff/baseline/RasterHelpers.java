package io.github.stellarsunset.tiff.baseline;

import mil.nga.tiff.Rasters;

public final class RasterHelpers {

    private RasterHelpers() {
    }

    public static byte[][] toByteRaster(Rasters rasters) {
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

    public static short[][] toShortRaster(Rasters rasters) {
        int w = rasters.getWidth();
        int h = rasters.getHeight();

        int componentsPerPixel = rasters.getBitsPerSample().size();
        short[][] shorts = new short[rasters.getHeight()][rasters.getWidth() * componentsPerPixel];

        for (int r = 0; r < h; r++) {
            short[] row = shorts[r];
            for (int c = 0; c < w; c++) {
                int offset = c * componentsPerPixel;
                Number[] numbers = rasters.getPixel(c, r);
                for (int comp = 0; comp < componentsPerPixel; comp++) {
                    row[offset + comp] = (short) (int) numbers[comp];
                }
            }
        }

        return shorts;
    }

    public static int[][] toIntRaster() {
        return new int[][]{};
    }

    public static float[][] toFloatRaster(Rasters rasters) {
        int w = rasters.getWidth();
        int h = rasters.getHeight();

        int componentsPerPixel = rasters.getBitsPerSample().size();
        float[][] floats = new float[rasters.getHeight()][rasters.getWidth() * componentsPerPixel];

        for (int r = 0; r < h; r++) {
            float[] row = floats[r];
            for (int c = 0; c < w; c++) {
                int offset = c * componentsPerPixel;
                Number[] numbers = rasters.getPixel(c, r);
                for (int comp = 0; comp < componentsPerPixel; comp++) {
                    row[offset + comp] = (float) numbers[comp];
                }
            }
        }

        return floats;
    }
}
