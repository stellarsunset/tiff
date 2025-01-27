package io.github.stellarsunset.tiff.compress;

import io.github.stellarsunset.tiff.BytesAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * This implementation is taken almost verbatim from the LZW Decoding appending of the TIFF 6.0 specification.
 *
 * <p>See <a href="https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf">here</a>.
 */
record Lzw() implements Compressor {

    /**
     * Clear code - i.e. reset and wipe the dictionary.
     */
    private static final short CLEAR_CODE = 256;

    /**
     * End of information code
     */
    private static final short EOI_CODE = 257;

    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        try {
            return decompressSafely(bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    private byte[] decompressSafely(byte[] bytes) throws IOException {
        ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();

        CodeTable table = new CodeTable();
        BitsInStream in = new BitsInStream(bytes);

        short previousCode = 0;
        short code = in.readBitsAsShort(table.codeBits());

        while (code != EOI_CODE) {
            if (code == CLEAR_CODE) {
                table = new CodeTable();

                code = in.readBitsAsShort(table.codeBits());
                if (code == EOI_CODE) {
                    break;
                }

                decodedStream.write(table.bytesForCode(code));
            } else {
                if (table.containsCode(code)) {
                    byte[] seq = table.bytesForCode(code);
                    decodedStream.write(seq);

                    byte[] newSeq = appendByte(table.bytesForCode(previousCode), seq[0]);
                    table.addNextCode(newSeq);
                } else {
                    byte[] seq = table.bytesForCode(previousCode);
                    byte[] newSeq = appendByte(seq, seq[0]);

                    decodedStream.write(newSeq);
                    table.addNextCode(newSeq);
                }
            }
            previousCode = code;
            code = in.readBitsAsShort(table.codeBits());
        }

        return decodedStream.toByteArray();
    }

    static byte[] appendByte(byte[] bytes, byte b) {
        byte[] a = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, a, 0, bytes.length);
        a[a.length - 1] = b;
        return a;
    }

    static final class CodeTable {
        /**
         * Mapping from a short, containing the 9-12 bit code
         */
        private final Map<Short, byte[]> table;
        /**
         * The current highest code value in the table.
         */
        private short currentMaxCode;
        /**
         * The number of bits required to represent the maximum code value.
         */
        private int codeBits;

        CodeTable() {
            this.table = initializeCodeTable();
            this.currentMaxCode = 257;
            this.codeBits = 9;
        }

        private static Map<Short, byte[]> initializeCodeTable() {
            Map<Short, byte[]> table = new HashMap<>();
            for (int i = 0; i <= 257; i++) {
                table.put((short) i, new byte[]{(byte) i});
            }
            return table;
        }

        public int codeBits() {
            return codeBits;
        }

        public boolean containsCode(short code) {
            return table.containsKey(code);
        }

        /**
         * Return the byte sequence associated with the current code in the code table.
         */
        public byte[] bytesForCode(short code) {
            return table.get(code);
        }

        /**
         * Add the provided bytes
         */
        public CodeTable addNextCode(byte[] bytes) {
            table.put(incrementMaxCode(), bytes);
            return this;
        }

        /**
         * Increment and return the new maximum code in the code table + check and update the current code bits.
         */
        private short incrementMaxCode() {
            currentMaxCode++;
            if (currentMaxCode >= 510) {
                codeBits = 10;
            }
            if (currentMaxCode >= 1022) {
                codeBits = 11;
            }
            if (currentMaxCode >= 2046) {
                codeBits = 12;
            }
            return currentMaxCode;
        }
    }

    static final class BitsInStream {

        private final byte[] bytes;

        private int totalBits;

        private int bitOffset;

        BitsInStream(byte[] bytes) {
            this.bytes = requireNonNull(bytes);
            this.totalBits = bytes.length * 8;
            this.bitOffset = 0;
        }

        int bitsRemaining() {
            return totalBits - bitOffset;
        }

        BitsInStream bitOffset(int newOffset) {
            this.bitOffset = newOffset;
            return this;
        }

        /**
         * A short is long enough to safely contain the 9-12 bit codes used to alias byte sequences in the file.
         *
         * <p>Force {@code [8, 16]} as the range to make the logic simpler.
         */
        short readBitsAsShort(int bitsToRead) {
            checkArgument(8 <= bitsToRead && bitsToRead <= 16);

            int i = Math.floorDiv(bitOffset, 8);
            int r = bitOffset - (i * 8);

            int lo = Byte.toUnsignedInt(bytes[i]);
            int mi = Byte.toUnsignedInt(bytes[i + 1]);

            // need to shift low left to make room to its right for the rest of bitsToRead
            int bitsFromLo = (8 - r);
            int loShift = bitsToRead - bitsFromLo;

            int bits = trimBitsLeftOf(lo, r) << loShift;

            if (loShift > 8) {

                int miShift = loShift - 8;
                bits = bits | (mi << miShift);

                int hi = Byte.toUnsignedInt(bytes[i + 2]);
                bits = bits | (hi >>> (8 - miShift));

            } else {
                bits = bits | (mi >>> (8 - loShift));
            }

            bitOffset += bitsToRead;
            return (short) bits;
        }

        static int trimBitsLeftOf(int value, int pos) {
            int shift = 24 + pos;
            return (value << shift) >>> shift;
        }
    }
}
