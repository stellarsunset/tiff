package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.nio.*;

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
     * {@link DifferencingPredictor} for data whose byte layout is naturally compressible by plain integer subtraction,
     * i.e. byte, short, int, long rasters.
     *
     * @param componentsPerPixel the number of components per pixel
     */
    static DifferencingPredictor horizontal(int componentsPerPixel) {
        return new Planar1Horizontal(componentsPerPixel);
    }

    /**
     * {@link DifferencingPredictor} for floating-point data where taking standard arithmetic differences doesn't result
     * in byte sequences which are more compressible (due how IEEE 754 orders the bytes).
     *
     * <p>Long story short the bytes of the floating-point numbers are re-ordered such that the exponent bits and mantissa
     * bits of different floating values are grouped together into contiguous ranges prior to differencing.
     *
     * <p>The basic algorithm is outlined in the <a href="http://chriscox.org/TIFFTN3d1.pdf">Adobe Technical Note 3</a>.
     *
     * <p>There is a nice image include in <a href="https://github.com/image-rs/image-tiff/issues/89">this PR</a>.
     *
     * @param componentsPerPixel the number of components per pixel
     */
    static DifferencingPredictor floatingPoint(int componentsPerPixel) {
        return new Planar1FloatingPoint(new Planar1Horizontal(componentsPerPixel));
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

        int componentsPerPixel = SamplesPerPixel.getRequired(ifd);

        return switch (type) {
            case 1 -> new Noop();
            case 2 -> new Planar1Horizontal(componentsPerPixel);
            case 3 -> new Planar1FloatingPoint(new Planar1Horizontal(componentsPerPixel));
            default -> throw new IllegalArgumentException(
                    String.format("Illegal differencing predictor type %s, should be 1, 2, or 3", type)
            );
        };
    }

    /**
     * Unpack the differenced bytes into proper values.
     *
     * <p>Run this on the raw image, which may be a raster of bytes/shorts or some other primitive type, after running LZW
     * decompression on it.
     *
     * @param buffer the underlying buffer of primitive data to unpack
     */
    void unpack(Buffer buffer);

    default void unpackAll(ByteOrder order, byte[][] bytes) {
        for (byte[] row : bytes) {
            unpack(ByteBuffer.wrap(row).order(order));
        }
    }

    /**
     * Repack the raw bytes into their differenced form.
     *
     * <p>Run this on the raw image, which may be a raster of bytes/shorts or some other primitive type, prior to running
     * LZW compression on it.
     *
     * @param buffer the underlying buffer of primitive data to pack
     */
    void pack(Buffer buffer);

    default void packAll(ByteOrder order, byte[][] bytes) {
        for (byte[] row : bytes) {
            pack(ByteBuffer.wrap(row).order(order));
        }
    }

    record Noop() implements DifferencingPredictor {
        @Override
        public void unpack(Buffer buffer) {
        }

        @Override
        public void pack(Buffer buffer) {
        }
    }

    record Planar1Horizontal(int componentsPerPixel) implements DifferencingPredictor {
        @Override
        public void unpack(Buffer buffer) {
            for (int component = 0; component < componentsPerPixel; component++) {
                int begin = buffer.position() + component + componentsPerPixel;
                for (int c = begin; c < buffer.limit(); c += componentsPerPixel) {
                    sum(buffer, c);
                }
            }
        }

        private void sum(Buffer buffer, int loc) {
            switch (buffer) {
                case ByteBuffer bBuffer -> {
                    byte b = bBuffer.get(loc);
                    b += bBuffer.get(loc - componentsPerPixel);
                    bBuffer.put(loc, b);
                }
                case CharBuffer cBuffer -> {
                    char c = cBuffer.get(loc);
                    c += cBuffer.get(loc - componentsPerPixel);
                    cBuffer.put(loc, c);
                }
                case ShortBuffer sBuffer -> {
                    short s = sBuffer.get(loc);
                    s += sBuffer.get(loc - componentsPerPixel);
                    sBuffer.put(loc, s);
                }
                case IntBuffer iBuffer -> {
                    int i = iBuffer.get(loc);
                    i += iBuffer.get(loc - componentsPerPixel);
                    iBuffer.put(loc, i);
                }
                case LongBuffer lBuffer -> {
                    long l = lBuffer.get(loc);
                    l += lBuffer.get(loc - componentsPerPixel);
                    lBuffer.put(loc, l);
                }
                case FloatBuffer _, DoubleBuffer _ -> throw new IllegalArgumentException(
                        "Standard horizontal differencing not supported for floating-point types."
                );
            }
        }

        @Override
        public void pack(Buffer buffer) {
            for (int component = 0; component < componentsPerPixel; component++) {
                int begin = (buffer.limit() - componentsPerPixel) + component;
                int end = component + buffer.position();
                for (int c = begin; c > end; c -= componentsPerPixel) {
                    difference(buffer, c);
                }
            }
        }

        private void difference(Buffer buffer, int loc) {
            switch (buffer) {
                case ByteBuffer bBuffer -> {
                    byte b = bBuffer.get(loc);
                    b -= bBuffer.get(loc - componentsPerPixel);
                    bBuffer.put(loc, b);
                }
                case CharBuffer cBuffer -> {
                    char c = cBuffer.get(loc);
                    c -= cBuffer.get(loc - componentsPerPixel);
                    cBuffer.put(loc, c);
                }
                case ShortBuffer sBuffer -> {
                    short s = sBuffer.get(loc);
                    s -= sBuffer.get(loc - componentsPerPixel);
                    sBuffer.put(loc, s);
                }
                case IntBuffer iBuffer -> {
                    int i = iBuffer.get(loc);
                    i -= iBuffer.get(loc - componentsPerPixel);
                    iBuffer.put(loc, i);
                }
                case LongBuffer lBuffer -> {
                    long l = lBuffer.get(loc);
                    l -= lBuffer.get(loc - componentsPerPixel);
                    lBuffer.put(loc, l);
                }
                case FloatBuffer _, DoubleBuffer _ -> throw new IllegalArgumentException(
                        "Standard horizontal differencing not supported for floating-point types."
                );
            }
        }
    }

    record Planar1FloatingPoint(Planar1Horizontal horizontal) implements DifferencingPredictor {

        @Override
        public void unpack(Buffer buffer) {
            if (buffer instanceof ByteBuffer bBuffer) {
                horizontal.unpack(bBuffer);

                int len = buffer.limit() - buffer.position();
                int quadrantSize = len / 4;

                int expHi = 0;
                int expLo = quadrantSize;
                int mantissaHi = 2 * quadrantSize;
                int mantissaLo = 3 * quadrantSize;

                ByteBuffer temp = ByteBuffer.allocate(len).order(bBuffer.order());
                for (int i = 0; i < quadrantSize; i++) {

                    byte expHiI = bBuffer.get(expHi + i);
                    byte expLoI = bBuffer.get(expLo + i);
                    byte mantissaHiI = bBuffer.get(mantissaHi + i);
                    byte mantissaLoI = bBuffer.get(mantissaLo + i);

                    int offset = i * 4;
                    if (bBuffer.order().equals(ByteOrder.LITTLE_ENDIAN)) {
                        temp.put(offset, mantissaLoI);
                        temp.put(offset + 1, mantissaHiI);
                        temp.put(offset + 2, expLoI);
                        temp.put(offset + 3, expHiI);
                    } else {
                        temp.put(offset, expHiI);
                        temp.put(offset + 1, expLoI);
                        temp.put(offset + 2, mantissaHiI);
                        temp.put(offset + 3, mantissaLoI);
                    }
                }

                for (int i = 0; i < len; i++) {
                    bBuffer.put(bBuffer.position() + i, temp.get(i));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("The floating-point predictor MUST be run on the raw re-ordered bytes, got %s", buffer.getClass().getSimpleName())
                );
            }
        }

        @Override
        public void pack(Buffer buffer) {

            horizontal.pack(buffer);
        }
    }
}
