package com.stellarsunset.tiff;

import com.stellarsunset.tiff.baseline.BaselineImage;
import com.stellarsunset.tiff.extension.ExtensionImage;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public sealed interface Image permits Image.Unknown, Image.Lazy, BaselineImage, ExtensionImage {

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
     * <p>Baseline {@link Image.Maker} implementations aggressively load the byte contents of the image on parse which may
     * not be desirable in situations with high memory pressure.
     *
     * <p>This wrapper allows us to hold onto the structured handles for these files ({@link TiffFile}) and make decisions
     * about whether we want to interact with them without requiring us to load all their image content.
     */
    static Image lazy(Supplier<Image> supplier) {
        return new Image.Lazy(supplier);
    }

    Pixel valueAt(int row, int col);

    record Unknown(SeekableByteChannel channel, Ifd ifd) implements Image {
        @Override
        public Pixel valueAt(int row, int col) {
            return new Pixel.Empty();
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
        public Pixel valueAt(int row, int col) {
            return delegate().valueAt(row, col);
        }
    }

    /**
     * Supplier-like interface for generating images from the bytes of a file and the IFD describing the image contents.
     *
     * <p>Most concrete image implementations will provide their own {@link Maker} implementation, these implementations
     * are then rolled up into broader implementations like the {@link BaselineImage.Maker}.
     *
     * <p>Many of these maker classes delegate most of the data-reading work directly to {@link Raster.Reader}, as there's
     * more diversity in the interpretation of the data in the raster (i.e. the {@link Image}) than there is in the data
     * itself.
     */
    @FunctionalInterface
    interface Maker {

        /**
         * Returns a new {@link Image.Maker} for {@link Image}s that supports all the {@link BaselineImage} image types.
         *
         * <p>By default to decrease memory pressure when loading a large number of TIFF files and images the baseline
         * maker returns lazy handles to the extracted image data.
         */
        static Maker baseline() {
            return new BaselineImage.Maker();
        }

        /**
         * Wraps the provided {@link Image.Maker} as one that produces {@link Image.Lazy} definitions.
         */
        static Maker lazy(Image.Maker maker) {
            return (channel, order, ifd) -> Image.lazy(() -> maker.makeImage(channel, order, ifd));
        }

        /**
         * Creates a new image based on the contents of the provided {@link Ifd} and the {@link SeekableByteChannel} pointing
         * to the underlying TIFF file.
         *
         * @param channel the open channel to the bytes of the file
         * @param order   the byte order to use when interpreting data in the underlying image
         * @param ifd     the image file directory ({@link Ifd}) with tags describing the contents of the image
         */
        Image makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd);
    }
}
