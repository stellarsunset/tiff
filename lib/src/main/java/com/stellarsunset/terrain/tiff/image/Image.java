package com.stellarsunset.terrain.tiff.image;

import com.stellarsunset.terrain.tiff.Ifd;
import com.stellarsunset.terrain.tiff.TiffFile;

import java.nio.channels.SeekableByteChannel;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public sealed interface Image {

    /**
     * Handle for an unknown image type detected in a file.
     *
     * <p>Allows the library to not throw if it doesn't know an image type, still returning a useful tiff file handle and
     * {@link Ifd} the client may use to interact with the image contents.
     */
    static Image unknown(SeekableByteChannel channel, Ifd ifd) {
        return new Image.Unknown(channel, ifd);
    }

    /**
     * Returns a new lazy-loading wrapper for an {@link Image}.
     *
     * <p>Baseline {@link ImageMaker} implementations aggressively load the byte contents of the image on parse which may
     * not be desirable in situations with high memory pressure.
     *
     * <p>This wrapper allows us to hold onto the structured handles for these files ({@link TiffFile}) and make decisions
     * about whether we want to interact with them without requiring us to load all their image content.
     */
    static Image lazy(Supplier<Image> supplier) {
        return new Image.Lazy(supplier);
    }

    PixelValue valueAt(int x, int y);

    record Unknown(SeekableByteChannel channel, Ifd ifd) implements Image {
        @Override
        public PixelValue valueAt(int row, int col) {
            return new PixelValue.Empty();
        }
    }

    final class Lazy implements Image {

        private final Supplier<Image> supplier;

        private volatile Image delegate;

        private Lazy(Supplier<Image> supplier) {
            this.supplier = requireNonNull(supplier);
        }

        /**
         * Access the underlying delegate image, potentially materializing it into main memory.
         */
        public Image delegate() {
            Image result = delegate;
            if (result == null) {
                synchronized (this) {
                    result = delegate;
                    if (result == null) {
                        this.delegate = result = supplier.get();
                    }
                }
            }
            return result;
        }

        @Override
        public PixelValue valueAt(int row, int col) {
            return delegate().valueAt(row, col);
        }
    }

    sealed interface Baseline extends Image permits BiLevelImage, GrayscaleImage, PaletteColorImage, RgbImage {
    }

    non-sealed interface Extension extends Image {
    }
}
