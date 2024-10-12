package com.stellarsunset.terrain.tiff.compress;

import com.stellarsunset.terrain.tiff.BytesAdapter;

@FunctionalInterface
public interface Compressor {

    /**
     * Assume no modification of the underlying binary data was made.
     */
    static Compressor noop() {
        return (bytes, order) -> bytes;
    }

    /**
     * No compression, but pack data into bytes as tightly as possible, leaving no unused bits (except at the end of a row).
     *
     * <p>The component values are stored as an array of type BYTE. Each scan line (row) is padded to the next BYTE boundary.
     */
    static Compressor uncompressed() {
        return new Uncompressed();
    }

    /**
     * CCITT Group 3 1-Dimensional Modified Huffman run-length encoding. See Section 10.
     *
     * <p>BitsPerSample must be 1, since this type of compression is defined only for BiLevel images.
     */
    static Compressor modifiedHuffman() {
        return new ModifiedHuffman();
    }

    /**
     * PackBits compression, a simple byte-oriented run-length scheme. See Section 9 for details.
     */
    static Compressor packBits() {
        return new PackBits();
    }

    /**
     * Decompresses the provided {@code byte[]} from its compressed form to its uncompressed one.
     */
    byte[] decompress(byte[] bytes, BytesAdapter adapter);
}
