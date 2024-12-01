package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.BufferView;
import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.Compression;
import com.stellarsunset.tiff.baseline.tag.SamplesPerPixel;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
            case 2 -> horizontal(componentsPerPixel);
            case 3 -> floatingPoint(componentsPerPixel);
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
    void unpack(BufferView buffer);

    /**
     * Repack the raw bytes into their differenced form.
     *
     * <p>Run this on the raw image, which may be a raster of bytes/shorts or some other primitive type, prior to running
     * LZW compression on it.
     *
     * @param buffer the underlying buffer of primitive data to pack
     */
    void pack(BufferView buffer);

    record Noop() implements DifferencingPredictor {
        @Override
        public void unpack(BufferView buffer) {
        }

        @Override
        public void pack(BufferView buffer) {
        }
    }

    record Planar1Horizontal(int componentsPerPixel) implements DifferencingPredictor {
        @Override
        public void unpack(BufferView buffer) {
            for (int component = 0; component < componentsPerPixel; component++) {
                int begin = component + componentsPerPixel;
                for (int c = begin; c < buffer.len(); c += componentsPerPixel) {
                    sum(buffer, c);
                }
            }
        }

        private void sum(BufferView buffer, int loc) {
            switch (buffer) {
                case BufferView.Byte bBuffer -> {
                    byte b = bBuffer.getByte(loc);
                    b += bBuffer.getByte(loc - componentsPerPixel);
                    bBuffer.putByte(loc, b);
                }
                case BufferView.Char cBuffer -> {
                    char c = cBuffer.getChar(loc);
                    c += cBuffer.getChar(loc - componentsPerPixel);
                    cBuffer.putChar(loc, c);
                }
                case BufferView.Short sBuffer -> {
                    short s = sBuffer.getShort(loc);
                    s += sBuffer.getShort(loc - componentsPerPixel);
                    sBuffer.putShort(loc, s);
                }
                case BufferView.Int iBuffer -> {
                    int i = iBuffer.getInt(loc);
                    i += iBuffer.getInt(loc - componentsPerPixel);
                    iBuffer.putInt(loc, i);
                }
                case BufferView.Long lBuffer -> {
                    long l = lBuffer.getLong(loc);
                    l += lBuffer.getLong(loc - componentsPerPixel);
                    lBuffer.putLong(loc, l);
                }
                case BufferView.Float _, BufferView.Double _ -> throw new IllegalArgumentException(
                        "Standard horizontal differencing not supported for floating-point types."
                );
            }
        }

        @Override
        public void pack(BufferView buffer) {
            for (int component = 0; component < componentsPerPixel; component++) {
                int begin = (buffer.len() - componentsPerPixel) + component;
                for (int c = begin; c > component; c -= componentsPerPixel) {
                    difference(buffer, c);
                }
            }
        }

        private void difference(BufferView buffer, int loc) {
            switch (buffer) {
                case BufferView.Byte bBuffer -> {
                    byte b = bBuffer.getByte(loc);
                    b -= bBuffer.getByte(loc - componentsPerPixel);
                    bBuffer.putByte(loc, b);
                }
                case BufferView.Char cBuffer -> {
                    char c = cBuffer.getChar(loc);
                    c -= cBuffer.getChar(loc - componentsPerPixel);
                    cBuffer.putChar(loc, c);
                }
                case BufferView.Short sBuffer -> {
                    short s = sBuffer.getShort(loc);
                    s -= sBuffer.getShort(loc - componentsPerPixel);
                    sBuffer.putShort(loc, s);
                }
                case BufferView.Int iBuffer -> {
                    int i = iBuffer.getInt(loc);
                    i -= iBuffer.getInt(loc - componentsPerPixel);
                    iBuffer.putInt(loc, i);
                }
                case BufferView.Long lBuffer -> {
                    long l = lBuffer.getLong(loc);
                    l -= lBuffer.getLong(loc - componentsPerPixel);
                    lBuffer.putLong(loc, l);
                }
                case BufferView.Float _, BufferView.Double _ -> throw new IllegalArgumentException(
                        "Standard horizontal differencing not supported for floating-point types."
                );
            }
        }
    }

    record Planar1FloatingPoint(Planar1Horizontal horizontal) implements DifferencingPredictor {

        @Override
        public void unpack(BufferView buffer) {
            if (buffer instanceof BufferView.Byte bBuffer) {
                horizontal.unpack(bBuffer);

                int len = bBuffer.lengthBytes();
                int quadrantSize = len / 4;

                int expHi = 0;
                int expLo = quadrantSize;
                int mantissaHi = 2 * quadrantSize;
                int mantissaLo = 3 * quadrantSize;

                ByteBuffer temp = ByteBuffer.allocate(len);
                for (int i = 0; i < quadrantSize; i++) {

                    byte expHiI = bBuffer.getByte(expHi + i);
                    byte expLoI = bBuffer.getByte(expLo + i);
                    byte mantissaHiI = bBuffer.getByte(mantissaHi + i);
                    byte mantissaLoI = bBuffer.getByte(mantissaLo + i);

                    int offset = i * 4;
                    if (bBuffer.delegate().order().equals(ByteOrder.LITTLE_ENDIAN)) {
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
                    bBuffer.putByte(i, temp.get(i));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("The floating-point predictor MUST be run on the raw re-ordered bytes, got %s", buffer.getClass().getSimpleName())
                );
            }
        }

        @Override
        public void pack(BufferView buffer) {
            throw new UnsupportedOperationException("Not yet implemented.");
        }
    }
}
