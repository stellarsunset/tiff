package io.github.stellarsunset.tiff;

import io.github.stellarsunset.tiff.baseline.BaselineImage;
import io.github.stellarsunset.tiff.baseline.BiLevelImage;
import io.github.stellarsunset.tiff.baseline.GrayscaleImage;
import io.github.stellarsunset.tiff.baseline.RgbImage;
import io.github.stellarsunset.tiff.baseline.tag.ColorMap;
import io.github.stellarsunset.tiff.extension.ExtensionImage;

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
            return java.lang.Byte.toUnsignedInt(value);
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
            return java.lang.Byte.toUnsignedInt(value);
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
            return java.lang.Byte.toUnsignedInt(value);
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
            return java.lang.Byte.toUnsignedInt(index);
        }

        public int unsignedR() {
            return java.lang.Short.toUnsignedInt(r);
        }

        public int unsignedG() {
            return java.lang.Short.toUnsignedInt(g);
        }

        public int unsignedB() {
            return java.lang.Short.toUnsignedInt(b);
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
            return java.lang.Byte.toUnsignedInt(r);
        }

        public int unsignedG() {
            return java.lang.Byte.toUnsignedInt(g);
        }

        public int unsignedB() {
            return java.lang.Byte.toUnsignedInt(b);
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

    sealed interface Byte extends Extension {
    }

    /**
     * Represents the value of a pixel as a single 8-bit integer (byte).
     *
     * <p>Most {@link Pixel.Baseline} types can be represented a {@link Byte1} or {@link Byte3} types.
     */
    record Byte1(byte value) implements Pixel.Byte {

        public int unsignedValue() {
            return java.lang.Byte.toUnsignedInt(value);
        }
    }

    /**
     * Represents the value of a pixel as a trio of 8-bit integer (byte) values.
     */
    record Byte3(byte b1, byte b2, byte b3) implements Pixel.Byte {

        public int unsignedB1() {
            return java.lang.Byte.toUnsignedInt(b1);
        }

        public int unsignedB2() {
            return java.lang.Byte.toUnsignedInt(b2);
        }

        public int unsignedB3() {
            return java.lang.Byte.toUnsignedInt(b3);
        }
    }

    /**
     * Represents the value of a pixel as an arbitrary number of 8-bit integer (byte) values.
     */
    record ByteN(byte[] values) implements Pixel.Byte {
    }


    sealed interface Short extends Extension {
    }

    /**
     * Represents the value of a pixel as a single 16-bit integer.
     *
     * <p>These are typically used in extensions for storing measurements (e.g. elevations) taken on grids as images.
     */
    record Short1(short value) implements Pixel.Short {

        public int unsignedValue() {
            return java.lang.Short.toUnsignedInt(value);
        }
    }

    /**
     * Represents the value of a pixel as a trio of 16-bit integer values.
     */
    record Short3(short s1, short s2, short s3) implements Pixel.Short {

        public int unsignedS1() {
            return java.lang.Short.toUnsignedInt(s1);
        }

        public int unsignedS2() {
            return java.lang.Short.toUnsignedInt(s2);
        }

        public int unsignedS3() {
            return java.lang.Short.toUnsignedInt(s3);
        }
    }

    /**
     * Represents the value of a pixel as an arbitrary number of 16-bit integer values.
     */
    record ShortN(short[] values) implements Pixel.Short {
    }

    sealed interface Int extends Extension {
    }

    /**
     * Represents the value of a pixel as a single 32-bit integer.
     *
     * <p>These are typically used in extensions for storing measurements (e.g. elevations) taken on grids as images.
     */
    record Int1(int value) implements Pixel.Int {

        public long unsignedValue() {
            return Integer.toUnsignedLong(value);
        }
    }

    /**
     * Represents the value of a pixel as a trio of 32-bit integer values.
     */
    record Int3(int i1, int i2, int i3) implements Pixel.Int {

        public long unsignedI1() {
            return java.lang.Integer.toUnsignedLong(i1);
        }

        public long unsignedI2() {
            return java.lang.Integer.toUnsignedLong(i2);
        }

        public long unsignedI3() {
            return java.lang.Integer.toUnsignedLong(i3);
        }
    }

    /**
     * Represents the value of a pixel as an arbitrary number of 32-bit integer values.
     */
    record IntN(int[] values) implements Pixel.Int {
    }

    sealed interface Float extends Extension {
    }

    /**
     * Represents the value of a pixel as a single 32-bit float.
     *
     * <p>These are typically used in extensions for storing measurements (e.g. elevations) taken on grids as images.
     */
    record Float1(float value) implements Pixel.Float {
    }

    /**
     * Represents the value of a pixel as a trio of 32-bit float values.
     */
    record Float3(float f1, float f2, float f3) implements Pixel.Float {
    }

    /**
     * Represents the value of a pixel as an arbitrary number of 32-bit float values.
     */
    record FloatN(float[] values) implements Pixel.Float {
    }
}
