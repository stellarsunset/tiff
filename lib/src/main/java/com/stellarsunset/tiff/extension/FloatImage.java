package com.stellarsunset.tiff.extension;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.Pixel;
import com.stellarsunset.tiff.Raster;
import com.stellarsunset.tiff.baseline.ImageDimensions;
import com.stellarsunset.tiff.baseline.Resolution;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

public record FloatImage(ImageDimensions dimensions, Resolution resolution, float[][] data) implements ExtensionImage {

    static Image.Maker maker() {
        return new Maker();
    }

    @Override
    public Pixel.Float valueAt(int row, int col) {
        return new Pixel.Float(data[row][col]);
    }

    record Maker() implements Image.Maker {

        @Override
        public FloatImage makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            Raster.Floats floats = new Raster.Reader.FloatStrips(1).readRaster(
                    channel,
                    order,
                    ifd
            );

            return new FloatImage(ImageDimensions.from(ifd), Resolution.from(ifd), floats.floats());
        }
    }
}
