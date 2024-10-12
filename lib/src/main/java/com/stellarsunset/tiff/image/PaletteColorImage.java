package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.BytesAdapter;
import com.stellarsunset.tiff.Ifd;

import java.nio.channels.SeekableByteChannel;

public record PaletteColorImage() implements Image.Baseline {

    static Maker maker(BytesAdapter adapter) {
        return new Maker(adapter);
    }

    @Override
    public PixelValue valueAt(int x, int y) {
        return new PixelValue.Empty();
    }

    record Maker(BytesAdapter adapter) implements ImageMaker {

        @Override
        public Image makeImage(SeekableByteChannel channel, Ifd ifd) {
            return Image.unknown(channel, ifd);
        }
    }
}
