package com.stellarsunset.tiff;

import com.stellarsunset.tiff.baseline.BaselineImage;
import com.stellarsunset.tiff.baseline.BiLevelImage;
import com.stellarsunset.tiff.baseline.GrayscaleImage;
import com.stellarsunset.tiff.baseline.RgbImage;
import com.stellarsunset.tiff.baseline.tag.ColorMap;
import com.stellarsunset.tiff.extension.ExtensionImage;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the value of a row/col (y/x) pixel in an image.
 *
 * <p>Different {@link Image} implementations are expected to return different {@link Pixel} subtypes specific to
 * their implementation.
 *
 * <p>These implementations are provided to:
 * <ol>
 *     <li>Provide distinct types to help communicate differences in how pixels are encoded</li>
 *     <li>Provide places for further documentation on their interpretation for rendering</li>
 * </ol>
 *
 * <p>We could place these inside the concrete image subtypes they're usually associated with, but they may be re-usable
 * between some image types. For now, leave them separate.
 */
public sealed interface Pixel {

    static Pixel empty() {
        return new Empty();
    }

    /**
     * Represents an empty pixel value.
     *
     * <p>Expected as a potential response type for out-of-range queries, testing, etc.
     */
    record Empty() implements Pixel {
    }

    /**
     * {@link Pixel} subtype for use in {@link BaselineImage} images.
     *
     * <p>Provided as a base for inheritance to allow easy switching over returned pixel values.
     */
    sealed interface Baseline extends Pixel {
    }

    /**
     * Bi-level image pixels are either black or white and can be stored as:
     * <ol>
     *     <li>{@link BiLevelImage.Interpretation#BLACK_IS_ZERO}</li>
     *     <li>{@link BiLevelImage.Interpretation#WHITE_IS_ZERO}</li>
     * </ol>
     *
     * <p>formats. This record encodes the raw pixel value and a boolean indicator for whether white is zero, it then
     * provides simple methods to determine whether the pixel should be colored white or black.
     *
     * <p>This could be broken into {@code WhiteIsZero} and {@code BlackIsZero} types, but that seems excessive.
     */
    record BlackOrWhite(byte value, boolean whiteIsZero) implements Pixel.Baseline {

        public BlackOrWhite {
            checkArgument(value == 0 || value == 1,
                    "Pixel value should be either 0 or 1, found %s.", value);
        }

        public boolean isWhite() {
            return whiteIsZero && value == 0 || !whiteIsZero && value == 1;
        }

        public boolean isBlack() {
            return whiteIsZero && value == 1 || !whiteIsZero && value == 0;
        }

        public int unsignedValue() {
            return Byte.toUnsignedInt(value);
        }
    }

    /**
     * Grayscale images can represent a finite number of "shades of gray" between white and black based on the number of
     * bits used in their encoding, in this case 4 (also see {@link Grayscale8}).
     *
     * <p>Grayscale images like Bi-level ones, can be encoded in:
     * <ol>
     *     <li>{@link GrayscaleImage.Interpretation#BLACK_IS_ZERO}</li>
     *     <li>{@link GrayscaleImage.Interpretation#WHITE_IS_ZERO}</li>
     * </ol>
     *
     * <p>formats. This record encodes only the raw pixel value and whether white should be interpreted as zero, the max
     * number of bits in the byte value used for tones is 15 (4 bits).
     */
    record Grayscale4(byte value, boolean whiteIsZero) implements Pixel.Baseline {

        public Grayscale4 {
            checkArgument(value >= 0 && value < 16, "4-bit grayscale range is [0, 16).");
        }

        public int unsignedValue() {
            return Byte.toUnsignedInt(value);
        }
    }

    /**
     * Grayscale images can represent a finite number of "shades of gray" between white and black based on the number of
     * bits used in their encoding, in this case 8 (also see {@link Grayscale4}).
     *
     * <p>Grayscale images like Bi-level ones, can be encoded in:
     * <ol>
     *     <li>{@link GrayscaleImage.Interpretation#BLACK_IS_ZERO}</li>
     *     <li>{@link GrayscaleImage.Interpretation#WHITE_IS_ZERO}</li>
     * </ol>
     *
     * <p>formats. This record encodes only the raw pixel value and whether white should be interpreted as zero. the max
     * number of bits in the byte value used for tones is 255 (8 bits).
     */
    record Grayscale8(byte value, boolean whiteIsZero) implements Pixel.Baseline {

        public int unsignedValue() {
            return Byte.toUnsignedInt(value);
        }
    }


    /**
     * Palette color image pixels are a byte value that can be used to index into a {@link ColorMap}, the color map then
     * contains the {@link Rgb} value of the pixel.
     *
     * <p>I.e. it's a way to encode a limited set of RGB values compactly. These colors can also have more gradation to
     * them as each component (R, G, or B) is 16 bits instead of 8 as in a standard {@link RgbImage}.
     *
     * <p>Remember these values are unsigned, so to compare actual values apply the correct unsigned Java conversion.
     *
     * @param index the index in the {@link ColorMap} of the pixel value
     * @param r     the red component from the {@link ColorMap}
     * @param g     the green component from the {@link ColorMap}
     * @param b     the blue component from the {@link ColorMap}
     */
    record PaletteColor(byte index, short r, short g, short b) implements Pixel.Baseline {

        public int unsignedIndex() {
            return Byte.toUnsignedInt(index);
        }

        public int unsignedR() {
            return Short.toUnsignedInt(r);
        }

        public int unsignedG() {
            return Short.toUnsignedInt(g);
        }

        public int unsignedB() {
            return Short.toUnsignedInt(b);
        }
    }

    /**
     * Represents the value of a pixel as a Red, Green, Blue set of components. In baseline RBG images each component is
     * only 8 bits, and so they can be carried through as bytes.
     *
     * <p>Remember these values are unsigned, so to compare actual values apply the correct unsigned Java conversion.
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */
    record Rgb(byte r, byte g, byte b) implements Pixel.Baseline {

        public int unsignedR() {
            return Short.toUnsignedInt(r);
        }

        public int unsignedG() {
            return Short.toUnsignedInt(g);
        }

        public int unsignedB() {
            return Short.toUnsignedInt(b);
        }
    }

    /**
     * {@link Pixel} subtype for use in {@link ExtensionImage} images.
     *
     * <p>Provided to allow external libraries to add their own pixel types without losing the sealed baseline type
     * hierarchy.
     */
    non-sealed interface Extension extends Pixel {

    }
}
