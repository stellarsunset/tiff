package com.stellarsunset.tiff.image;

import com.stellarsunset.tiff.tag.ColorMap;

/**
 * Represents the value of an X/Y pixel in an image.
 *
 * <p>Different {@link Image} implementations are expected to return different {@link PixelValue} subtypes specific to
 * their implementation.
 */
public sealed interface PixelValue {

    /**
     * Represents an empty pixel value.
     *
     * <p>Expected as a potential response type for out-of-range queries, testing, etc.
     */
    record Empty() implements PixelValue {
    }

    /**
     * {@link PixelValue} subtype for use in {@link Image.Baseline} images.
     *
     * <p>Provided as a base for inheritance to allow easy switching over returned pixel values.
     */
    sealed interface Baseline extends PixelValue {
    }

    /**
     *
     */
    record BlackOrWhite(byte value) implements PixelValue.Baseline {

        int unsignedValue() {
            return Byte.toUnsignedInt(value);
        }
    }

    /**
     *
     */
    record Grayscale(byte value) implements PixelValue.Baseline {

        int unsignedValue() {
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
    record PaletteColor(byte index, short r, short g, short b) implements PixelValue.Baseline {

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
    record Rgb(byte r, byte g, byte b) implements PixelValue.Baseline {

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
     * {@link PixelValue} subtype for use in {@link Image.Extension} images.
     *
     * <p>Provided to allow external libraries to add their own pixel types without losing the sealed baseline type
     * hierarchy.
     */
    non-sealed interface Extension extends PixelValue {

    }
}
