package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.Pixel;
import com.stellarsunset.tiff.Raster;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

/**
 * Grayscale images are a generalization of bi-level images. Bi-level images can store only black and white image data,
 * but grayscale images can also store shades of gray.
 *
 * <p>Baseline grayscale images allow for 4 or 8-bit shades of gray, i.e. 16 shades of gray or 256 shades of gray, each
 * pixel is only one byte.
 *
 * <p>Grayscale images are almost always uncompressed or PackBits compressed, usually PackBits performs poorly on them,
 * and so they are left uncompressed.
 */
public sealed interface GrayscaleImage extends BaselineImage {

    static Maker maker() {
        return new Maker();
    }

    /**
     * A grayscale image with 4-bit grayscale tones.
     */
    record Grayscale4Image(Interpretation type, ImageDimensions dimensions, Resolution resolution,
                           byte[][] data) implements GrayscaleImage {

        public Grayscale4Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel.Grayscale4 valueAt(int row, int col) {
            return new Pixel.Grayscale4(data[row][col], type.whiteIsZero());
        }
    }

    /**
     * A grayscale image with 8-bit grayscale tones.
     */
    record Grayscale8Image(Interpretation type, ImageDimensions dimensions, Resolution resolution,
                           byte[][] data) implements GrayscaleImage {

        public Grayscale8Image {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel.Grayscale8 valueAt(int row, int col) {
            return new Pixel.Grayscale8(data[row][col], type.whiteIsZero());
        }
    }

    /**
     * Allowable values for Baseline TIFF grayscale images are 4 and 8, allowing either 16 or 256 distinct shades of gray.
     */
    enum ShadesOfGray {
        N16,
        N256;

        private static ShadesOfGray from(Ifd ifd) {
            int bitsPerSample = BitsPerSample.getRequired(ifd)[0];
            return switch (bitsPerSample) {
                case 4 -> ShadesOfGray.N16;
                case 8 -> ShadesOfGray.N256;
                default ->
                        throw new IllegalArgumentException("Non-baseline BitsPerSample for Grayscale image: " + bitsPerSample);
            };
        }
    }

    enum Interpretation {
        /**
         * BlackIsZero. For bi-level and grayscale images: 0 is imaged as black. The maximum value is imaged as white.
         *
         * <p>If this value is specified for Compression=2, the image should display and print reversed.
         */
        BLACK_IS_ZERO,
        /**
         * WhiteIsZero. For bi-level and grayscale images: 0 is imaged as white. The maximum value is imaged as black.
         *
         * <p>This is the normal value for Compression=2.
         */
        WHITE_IS_ZERO;

        public boolean whiteIsZero() {
            return this.equals(WHITE_IS_ZERO);
        }

        private static Interpretation from(Ifd ifd) {
            int photometricCode = PhotometricInterpretation.getRequired(ifd);
            return switch (photometricCode) {
                case 0 -> Interpretation.WHITE_IS_ZERO;
                case 1 -> Interpretation.BLACK_IS_ZERO;
                default ->
                        throw new IllegalArgumentException("Unknown PhotometricInterpretation for Grayscale image: " + photometricCode);
            };
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public GrayscaleImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            Raster.Bytes bytes = Raster.Reader.bytes(1).readRaster(
                    channel,
                    order,
                    ifd
            );

            return switch (ShadesOfGray.from(ifd)) {
                case N16 -> new Grayscale4Image(
                        Interpretation.from(ifd),
                        ImageDimensions.from(ifd),
                        Resolution.from(ifd),
                        bytes.bytes()
                );
                case N256 -> new Grayscale8Image(
                        Interpretation.from(ifd),
                        ImageDimensions.from(ifd),
                        Resolution.from(ifd),
                        bytes.bytes()
                );
            };
        }
    }
}
