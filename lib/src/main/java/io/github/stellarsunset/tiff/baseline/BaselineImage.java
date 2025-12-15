package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.Pixel;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.BitsPerSample;
import io.github.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;
import io.github.stellarsunset.tiff.extension.DataImage;
import io.github.stellarsunset.tiff.extension.ExtensionImage;

import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.function.Supplier;

/**
 * Seals image types defined as "baseline" in the TIFF 6.0 specification, these are the image types <em>all</em> TIFF
 * file parsers are supposed to support.
 *
 * <p>See also {@link ExtensionImage} and {@link DataImage} for image types that are TIFF-compliant but not part of the
 * baseline TIFF 6.0 specification.
 */
public sealed interface BaselineImage extends Image permits BiLevelImage, GrayscaleImage, PaletteColorImage, RgbImage {

    static Image.Maker maker() {
        return new Maker();
    }

    @Override
    Pixel.Baseline valueAt(int row, int col);

    record Maker(Image.Maker biLevel, Image.Maker grayscale, Image.Maker fullColor,
                 Image.Maker palette) implements Image.Maker {

        public Maker() {
            this(
                    BiLevelImage.maker(),
                    GrayscaleImage.maker(),
                    RgbImage.maker(),
                    PaletteColorImage.maker()
            );
        }

        @Override
        public Image makeImage(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {

            int photometricCode = PhotometricInterpretation.get(ifd);

            Supplier<Image> supplier = () -> switch (photometricCode) {
                case 0, 1 -> grayscaleOrBiLevel(channel, order, ifd);
                case 2 -> fullColor.makeImage(channel, order, ifd);
                case 3 -> palette.makeImage(channel, order, ifd);
                default -> new Image.Unknown(channel, ifd);
            };

            return Image.lazy(supplier);
        }

        private Image grayscaleOrBiLevel(SeekableByteChannel channel, ByteOrder order, Ifd ifd) {
            return BitsPerSample.getIfPresent(ifd)
                    .filter(bps -> bps.length == 1 && (bps[0] == 4 || bps[0] == 8))
                    .map(_ -> grayscale.makeImage(channel, order, ifd))
                    .orElseGet(() -> biLevel.makeImage(channel, order, ifd));
        }
    }

    /**
     * Container class for all {@link Tag}s associated with {@link BaselineImage}s.
     */
    final class Tags {

        private Tags() {
        }


    }
}

