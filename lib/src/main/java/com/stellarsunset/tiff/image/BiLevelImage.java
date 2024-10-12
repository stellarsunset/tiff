package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.BytesAdapter;
import com.stellarsunset.tiff.BytesReader;
import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;
import com.stellarsunset.tiff.tag.Compression;
import com.stellarsunset.tiff.tag.PhotometricInterpretation;

import java.nio.ByteBuffer;
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
                           byte[][] data) implements Image.Baseline {

    public BiLevelImage {
        checkArgument(data.length == dimensions.imageLength(),
                "Expected %s rows, found %s", dimensions.imageLength(), data.length);

        checkArgument(data[0].length == dimensions.imageWidth(),
                "Expected %s columns, found %s", dimensions.imageWidth(), data[0].length);
    }

    static Maker maker(BytesAdapter adapter) {
        return new Maker(adapter);
    }

    @Override
    public PixelValue valueAt(int row, int col) {
        byte value = data[col][row];
        return new PixelValue.Empty();
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

    record Maker(BytesAdapter adapter) implements ImageMaker {

        @Override
        public Image makeImage(SeekableByteChannel channel, Ifd ifd) {

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
