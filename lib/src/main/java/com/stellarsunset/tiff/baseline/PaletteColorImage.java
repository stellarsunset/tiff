package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.tag.ColorMap;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Palette-color images are similar to grayscale images. They still have one component per pixel, but the component value
 * is used as an index into a full RGB-lookup table, i.e. a {@link ColorMap}.
 *
 * <p>See {@link Pixel.PaletteColor} for more info.
 */
public record PaletteColorImage(ImageDimensions dimensions, StripInfo stripInfo, Resolution resolution,
                                ColorMap colorMap, byte[][] data) implements BaselineImage {

    static Maker maker(BytesAdapter adapter) {
        return new Maker(adapter);
    }

    public PaletteColorImage {
        dimensions.checkBounds(data, 1);
    }

    @Override
    public Pixel.PaletteColor valueAt(int row, int col) {

        byte index = data[row][col];

        ColorMap.Rgb rgb = colorMap.rgb(Byte.toUnsignedInt(index));

        return new Pixel.PaletteColor(
                index,
                rgb.r(),
                rgb.g(),
                rgb.b()
        );
    }

    record Maker(BytesAdapter adapter) implements Image.Maker {

        @Override
        public PaletteColorImage makeImage(SeekableByteChannel channel, Ifd ifd) {

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

            return new PaletteColorImage(
                    imageDimensions,
                    stripInfo,
                    Resolution.from(ifd),
                    ColorMap.getRequired(ifd),
                    bytes
            );
        }
    }
}
