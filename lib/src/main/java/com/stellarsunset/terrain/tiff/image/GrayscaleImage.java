package com.stellarsunset.terrain.tiff.image;

import com.stellarsunset.terrain.tiff.BytesAdapter;
import com.stellarsunset.terrain.tiff.BytesReader;
import com.stellarsunset.terrain.tiff.Ifd;
import com.stellarsunset.terrain.tiff.compress.Compressor;
import com.stellarsunset.terrain.tiff.compress.Compressors;
import com.stellarsunset.terrain.tiff.tag.BitsPerSample;
import com.stellarsunset.terrain.tiff.tag.Compression;
import com.stellarsunset.terrain.tiff.tag.PhotometricInterpretation;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Grayscale images are a generalization of bi-level images. Bi-level images can store only black and white image data,
 * but grayscale images can also store shades of gray.
 *
 * <p>Grayscale images are almost always uncompressed or PackBits compressed, usually PackBits performs poorly on them,
 * and so they are left uncompressed.
 */
public record GrayscaleImage(Interpretation type, ImageDimensions dimensions, StripInfo stripInfo,
                             Resolution resolution, ShadesOfGray shadesOfGray,
                             byte[][] data) implements Image.Baseline {

    static Maker maker(BytesAdapter adapter) {
        return new Maker(adapter);
    }

    public GrayscaleImage {
        checkArgument(data.length == dimensions.imageLength(),
                "Expected %s rows, found %s", dimensions.imageLength(), data.length);

        checkArgument(data[0].length == dimensions.imageWidth(),
                "Expected %s columns, found %s", dimensions.imageWidth(), data[0].length);
    }

    @Override
    public PixelValue valueAt(int row, int col) {
        byte value = data[row][col];
        return new PixelValue.Empty();
    }

    /**
     * Allowable values for Baseline TIFF grayscale images are 4 and 8, allowing either
     * 16 or 256 distinct shades of gray.
     */
    public enum ShadesOfGray {
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
                        throw new IllegalArgumentException("Unknown PhotometricInterpretation for Grayscale image: " + photometricCode);
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

            return new GrayscaleImage(
                    Interpretation.from(ifd),
                    imageDimensions,
                    stripInfo,
                    Resolution.from(ifd),
                    ShadesOfGray.from(ifd),
                    bytes
            );
        }
    }
}
