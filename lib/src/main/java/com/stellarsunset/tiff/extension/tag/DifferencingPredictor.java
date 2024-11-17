package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.Raster;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A predictor is a mathematical operator that is applied to the image data before an encoding scheme is applied.
 *
 * <p>Currently this field is used only with LZW ({@link Compression}=5) encoding because LZW is probably the only TIFF
 * encoding scheme that benefits significantly from a predictor step.
 */
public interface DifferencingPredictor {

    String NAME = "DIFFERENCING_PREDICTOR";

    short ID = 0x13D;

    static DifferencingPredictor noop() {
        return new Noop();
    }

    /**
     * {@link DifferencingPredictor} for {@code byte[][]} data in
     * <ol>
     *     <li>{@link PlanarConfiguration} is one</li>
     *     <li>One byte components</li>
     *     <li>An arbitrary number of components per pixel, usually 1 (grayscale) or 3 (RGB)</li>
     * </ol>
     */
    static DifferencingPredictor planarOneByteComponents(int componentsPerPixel) {
        return new PlanarOneByteComponents(componentsPerPixel);
    }

    /**
     * Return the {@link DifferencingPredictor} which should be applied ot the image data post decompression but prior
     * to interpretation as pixel values (if applicable).
     *
     * <p>{@link DifferencingPredictor}s are only supported for images where each component is one byte, though there may
     * be an arbitrary number of components per pixel. This covers most grayscale and RGB images.
     *
     * @param ifd the {@link Ifd} to extract the required parameters from to create the predictor
     */
    static DifferencingPredictor get(Ifd ifd) {

        int planarConfiguration = PlanarConfiguration.getRequired(ifd);

        checkArgument(planarConfiguration == 1,
                "Predictors only supported on PlanarConfiguration 1, was %s", planarConfiguration);

        int type = switch (ifd.findTag(ID)) {
            case Entry.Short s -> Short.toUnsignedInt(s.values()[0]);
            case Entry.NotFound _ -> 1;
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                 Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                    throw new UnsupportedTypeForTagException(NAME, ID);
        };

        return switch (type) {
            case 1 -> new Noop();
            case 2 -> new PlanarOneByteComponents(SamplesPerPixel.getRequired(ifd));
            case 3 -> throw new IllegalArgumentException("Floating point prediction not supported.");
            default -> throw new IllegalArgumentException(
                    String.format("Illegal differencing predictor type %s, should be 1 or 2", type)
            );
        };
    }

    /**
     * Unpack the differenced bytes into proper values.
     *
     * <p>I.e. run this on the {@code byte[][]} returned by a {@link Raster.Reader} prior to handing it off to an Image
     * decorator.
     *
     * @param bytes uncompressed {@link Raster} bytes to un-difference.
     * @param start the start offset in the array to begin unpacking from
     * @param len   the number of bytes to unpack beyond the start
     */
    void unpack(byte[] bytes, int start, int len);

    default void unpack(byte[] bytes) {
        unpack(bytes, 0, bytes.length);
    }

    default void unpackAll(byte[][] bytes) {
        for (byte[] row : bytes) {
            unpack(row);
        }
    }

    /**
     * Repack the raw bytes into their differenced form.
     *
     * <p>Run this on the raw image {@link byte[][]} prior to re-compressing it with the LZW compressor.
     *
     * @param bytes the raw bytes of an image we want to differentially encode
     * @param start the start offset in the array to begin packing from
     * @param len   the number of bytes to unpack beyond the start
     */
    void pack(byte[] bytes, int start, int len);

    default void pack(byte[] bytes) {
        pack(bytes, 0, bytes.length);
    }


    default void packAll(byte[][] bytes) {
        for (byte[] row : bytes) {
            pack(row, 0, row.length);
        }
    }

    record Noop() implements DifferencingPredictor {
        @Override
        public void unpack(byte[] bytes, int start, int end) {
        }

        @Override
        public void pack(byte[] bytes, int start, int end) {
        }
    }

    record PlanarOneByteComponents(int componentsPerPixel) implements DifferencingPredictor {
        @Override
        public void unpack(byte[] bytes, int start, int len) {
            int end = start + len;
            for (int component = 0; component < componentsPerPixel; component++) {
                int begin = start + component + componentsPerPixel;
                for (int c = begin; c < end; c += componentsPerPixel) {
                    bytes[c] += bytes[c - componentsPerPixel];
                }
            }
        }

        @Override
        public void pack(byte[] bytes, int start, int len) {
            int end = start + len;
            for (int component = 0; component < componentsPerPixel; component++) {
                int begin = (end - componentsPerPixel) + component;
                for (int c = begin; c > component + start; c -= componentsPerPixel) {
                    bytes[c] -= bytes[c - componentsPerPixel];
                }
            }
        }
    }
}
