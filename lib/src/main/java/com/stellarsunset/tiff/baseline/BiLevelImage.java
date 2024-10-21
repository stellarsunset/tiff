package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Standard bi-level black and white image.
 *
 * <p>Pixel values indicate whether a cell in the image should be colored as black or white.
 *
 * <p>Bi-level images are almost always either uncompressed or compressed with PackBits, which is particularly effective
 * on them.
 */
public record BiLevelImage(Interpretation type, ImageDimensions dimensions, StripInfo stripInfo, Resolution resolution,
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

            BytesAdapter adapter = BytesAdapter.of(order);
            BytesReader reader = new BytesReader(channel);

            Compressor compressor = Compressors.getInstance()
                    .compressorFor(Compression.getRequired(ifd));

            ImageDimensions imageDimensions = ImageDimensions.from(ifd);
            StripInfo stripInfo = StripInfo.from(ifd);

            ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
            StripInfo.Int intStripInfo = stripInfo.asIntInfo();

            byte[][] bytes = new byte[intImageDimensions.imageLength()][intImageDimensions.imageWidth()];

            int nOffsets = stripInfo.stripOffsets().length;
            int rowsPerStrip = intStripInfo.rowsPerStrip();

            int imageWidth = intImageDimensions.imageWidth();

            for (int i = 0; i < nOffsets; i++) {

                long stripOffset = stripInfo.stripOffsets()[i];
                int stripBytes = intStripInfo.stripByteCounts()[i];

                ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                int rowsInStrip = uncompressedStrip.length / imageWidth;
                if (i != nOffsets - 1) {
                    checkArgument(rowsInStrip == rowsPerStrip,
                            "Incorrect number of rows found (%s) in strip# (%s).", rowsInStrip, i);
                }

                for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                    int imageRow = i * rowsPerStrip + stripRow;
                    int stripRowStart = stripRow * imageWidth;

                    bytes[imageRow] = Arrays.copyOfRange(uncompressedStrip, stripRowStart, stripRowStart + imageWidth);
                }
            }

            return new BiLevelImage(
                    Interpretation.from(ifd),
                    imageDimensions,
                    stripInfo,
                    Resolution.from(ifd),
                    bytes
            );
        }
    }
}
