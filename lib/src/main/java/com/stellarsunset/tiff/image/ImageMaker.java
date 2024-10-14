package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.BytesAdapter;
import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.tag.BitsPerSample;
import com.stellarsunset.tiff.tag.PhotometricInterpretation;

import java.nio.channels.SeekableByteChannel;
import java.util.function.Supplier;

public interface ImageMaker {

    /**
     * Returns a new {@link ImageMaker} for {@link Image}s that supports all the {@link Image.Baseline} image types.
     *
     * <p>By default to decrease memory pressure when loading a large number of TIFF files and images the baseline
     * maker returns lazy handles to the extracted image data.
     */
    static ImageMaker baseline(BytesAdapter adapter) {
        return new Baseline(adapter);
    }

    /**
     * Creates a new image based on the contents of the provided {@link Ifd} and the {@link SeekableByteChannel} pointing
     * to the underlying TIFF file.
     *
     * @param channel the open channel to the bytes of the file
     * @param ifd     the image file directory with tags describing the contents of the image
     */
    Image makeImage(SeekableByteChannel channel, Ifd ifd);

    record Baseline(ImageMaker biLevel, ImageMaker grayscale, ImageMaker fullColor,
                    ImageMaker palette) implements ImageMaker {

        public Baseline(BytesAdapter adapter) {
            this(
                    BiLevelImage.maker(adapter),
                    GrayscaleImage.maker(adapter),
                    RgbImage.maker(adapter),
                    PaletteColorImage.maker(adapter)
            );
        }

        @Override
        public Image makeImage(SeekableByteChannel channel, Ifd ifd) {

            int photometricCode = PhotometricInterpretation.getRequired(ifd);

            Supplier<Image> supplier = () -> switch (photometricCode) {
                case 0, 1 -> grayscaleOrBiLevel(channel, ifd);
                case 2 -> fullColor.makeImage(channel, ifd);
                case 3 -> palette.makeImage(channel, ifd);
                default -> new Image.Unknown(channel, ifd);
            };

            return Image.lazy(supplier);
        }

        private Image grayscaleOrBiLevel(SeekableByteChannel channel, Ifd ifd) {
            return BitsPerSample.getOptionalValue(ifd)
                    .filter(bps -> bps.length == 1 && (bps[0] == 4 || bps[0] == 8))
                    .map(_ -> grayscale.makeImage(channel, ifd))
                    .orElseGet(() -> biLevel.makeImage(channel, ifd));
        }
    }
}
