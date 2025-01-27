package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Pixel;
import io.github.stellarsunset.tiff.Raster;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

/**
 * Standard bi-level black and white image.
 *
 * <p>Pixel values indicate whether a cell in the image should be colored as black or white.
 *
 * <p>Bi-level images are almost always either uncompressed or compressed with PackBits, which is particularly effective
 * on them.
 */
public record BiLevelImage(Interpretation type, ImageDimensions dimensions, Resolution resolution,
                           byte[][] data) implements BaselineImage {

    public BiLevelImage {
        dimensions.checkBounds(data, 1);
    }

    static Maker maker() {
        return new Maker();
    }

    @Override
    public Pixel.BlackOrWhite valueAt(int row, int col) {
        return new Pixel.BlackOrWhite(data[row][col], type.whiteIsZero());
    }

    public enum Interpretation {
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
                        throw new IllegalArgumentException("Unknown PhotometricInterpretation for BiLevel image: " + photometricCode);
            };
        }
    }

    record Maker() implements Image.Maker {

        @Override
        public BiLevelImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            Raster.Bytes bytes = Raster.Reader.bytes(1).readRaster(
                    channel,
                    order,
                    ifd
            );

            return new BiLevelImage(
                    Interpretation.from(ifd),
                    ImageDimensions.get(ifd),
                    Resolution.from(ifd),
                    bytes.bytes()
            );
        }
    }
}
