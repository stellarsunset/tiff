package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.BytesAdapter;
import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Image;
import com.stellarsunset.tiff.baseline.tag.BitsPerSample;
import com.stellarsunset.tiff.baseline.tag.PhotometricInterpretation;

import java.nio.channels.SeekableByteChannel;
import java.util.function.Supplier;

/**
 * Seals image types defined as "baseline" in the TIFF 6.0 specification, these are the image types <em>all</em> TIFF
 * file parsers are supposed to support.
 *
 * <p>Baseline vs Extension was introduced as a way to introduce some predictability into what's any otherwise almost
 * arbitrarily extensible specification and align on at least some standard image layouts/types all parsers support.
 */
public sealed interface BaselineImage extends Image permits BiLevelImage, GrayscaleImage, PaletteColorImage, RgbImage {

    record Maker(Image.Maker biLevel, Image.Maker grayscale, Image.Maker fullColor,
                 Image.Maker palette) implements Image.Maker {

        public Maker(BytesAdapter adapter) {
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

