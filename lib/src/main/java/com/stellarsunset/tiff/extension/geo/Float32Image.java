package com.stellarsunset.tiff.extension.geo;

import com.stellarsunset.tiff.*;
import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.Resolution;
import com.stellarsunset.tiff.baseline.StripInfo;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.compress.Compressor;
import com.stellarsunset.tiff.compress.Compressors;
import com.stellarsunset.tiff.extension.ExtensionImage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import static com.google.common.base.Preconditions.checkArgument;

public record Float32Image(ImageDimensions dimensions, StripInfo stripInfo, Resolution resolution,
                           float[][] data) implements ExtensionImage {

    public static Image.Maker maker() {
        return new Maker();
    }

    @Override
    public Pixel.Float32 valueAt(int row, int col) {
        return new Pixel.Float32(data[row][col]);
    }

    record Maker() implements Image.Maker {

        @Override
        public Image makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            BytesAdapter adapter = BytesAdapter.of(order);
            ArrayBytesAdapter arrayAdapter = ArrayBytesAdapter.of(order);

            BytesReader reader = new BytesReader(channel);

            Compressor compressor = Compressors.getInstance()
                    .compressorFor(Compression.getRequired(ifd));

            ImageDimensions imageDimensions = ImageDimensions.from(ifd);
            StripInfo stripInfo = StripInfo.from(ifd);

            ImageDimensions.Int intImageDimensions = imageDimensions.asIntInfo();
            StripInfo.Int intStripInfo = stripInfo.asIntInfo();

            float[][] bytes = new float[intImageDimensions.imageLength()][intImageDimensions.imageWidth()];

            int nOffsets = stripInfo.stripOffsets().length;
            int rowsPerStrip = intStripInfo.rowsPerStrip();

            int imageWidth = intImageDimensions.imageWidth();
            int widthBytes = imageWidth * 4;

            for (int i = 0; i < nOffsets; i++) {

                long stripOffset = stripInfo.stripOffsets()[i];
                int stripBytes = intStripInfo.stripByteCounts()[i];

                ByteBuffer buffer = reader.readBytes(stripOffset, stripBytes);
                byte[] uncompressedStrip = compressor.decompress(buffer.array(), adapter);

                int rowsInStrip = uncompressedStrip.length / imageWidth / 4;
                if (i != nOffsets - 1) {
                    checkArgument(rowsInStrip == rowsPerStrip,
                            "Incorrect number of rows found (%s) in strip# (%s).", rowsInStrip, i);
                }

                for (int stripRow = 0; stripRow < rowsInStrip; stripRow++) {

                    int imageRow = i * rowsPerStrip + stripRow;
                    int stripRowStart = stripRow * widthBytes;

                    bytes[imageRow] = arrayAdapter.readFloats(
                            ByteBuffer.wrap(uncompressedStrip, stripRowStart, stripRowStart + widthBytes),
                            0,
                            imageWidth
                    );
                }
            }

            return new Float32Image(
                    imageDimensions,
                    stripInfo,
                    Resolution.from(ifd),
                    bytes
            );
        }
    }
}
