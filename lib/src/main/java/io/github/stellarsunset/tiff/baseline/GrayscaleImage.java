package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static com.google.common.base.Preconditions.checkArgument;

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
    record FourBit(Interpretation type, ImageDimensions dimensions, Resolution resolution,
                   byte[][] data) implements GrayscaleImage {

        public FourBit {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel(data[row][col], type.whiteIsZero());
        }

        /**
         * Grayscale images can represent a finite number of "shades of gray" between white and black based on the number of
         * bits used in their encoding, in this case 4 (also see {@link EightBit.Pixel}).
         *
         * <p>Grayscale images like Bi-level ones, can be encoded in:
         * <ol>
         *     <li>{@link GrayscaleImage.Interpretation#BLACK_IS_ZERO}</li>
         *     <li>{@link GrayscaleImage.Interpretation#WHITE_IS_ZERO}</li>
         * </ol>
         *
         * <p>formats. This record encodes only the raw pixel value and whether white should be interpreted as zero, the max
         * number of bits in the byte value used for tones is 15 (4 bits).
         */
        public record Pixel(byte value, boolean whiteIsZero) implements BaselineImage.Pixel {
            public Pixel {
                checkArgument(value >= 0 && value < 16, "4-bit grayscale range is [0, 16).");
            }

            public int unsignedValue() {
                return java.lang.Byte.toUnsignedInt(value);
            }
        }
    }

    /**
     * A grayscale image with 8-bit grayscale tones.
     */
    record EightBit(Interpretation type, ImageDimensions dimensions, Resolution resolution,
                    byte[][] data) implements GrayscaleImage {

        public EightBit {
            dimensions.checkBounds(data, 1);
        }

        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel(data[row][col], type.whiteIsZero());
        }

        /**
         * Grayscale images can represent a finite number of "shades of gray" between white and black based on the number of
         * bits used in their encoding, in this case 8 (also see {@link FourBit.Pixel}).
         *
         * <p>Grayscale images like Bi-level ones, can be encoded in:
         * <ol>
         *     <li>{@link GrayscaleImage.Interpretation#BLACK_IS_ZERO}</li>
         *     <li>{@link GrayscaleImage.Interpretation#WHITE_IS_ZERO}</li>
         * </ol>
         *
         * <p>formats. This record encodes only the raw pixel value and whether white should be interpreted as zero. the max
         * number of bits in the byte value used for tones is 255 (8 bits).
         */
        public record Pixel(byte value, boolean whiteIsZero) implements BaselineImage.Pixel {

            public int unsignedValue() {
                return java.lang.Byte.toUnsignedInt(value);
            }
        }
    }

    /**
     * Allowable values for Baseline TIFF grayscale images are 4 and 8, allowing either 16 or 256 distinct shades of gray.
     */
    enum ShadesOfGray {
        N16,
        N256;

        private static ShadesOfGray from(Ifd ifd) {
            int bitsPerSample = BitsPerSample.get(ifd)[0];
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
            int photometricCode = PhotometricInterpretation.get(ifd);
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
                case N16 -> new FourBit(
                        Interpretation.from(ifd),
                        ImageDimensions.get(ifd),
                        Resolution.from(ifd),
                        bytes.bytes()
                );
                case N256 -> new EightBit(
                        Interpretation.from(ifd),
                        ImageDimensions.get(ifd),
                        Resolution.from(ifd),
                        bytes.bytes()
                );
            };
        }
    }
}
