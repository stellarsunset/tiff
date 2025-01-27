package io.github.stellarsunset.tiff.compress;

import io.github.stellarsunset.tiff.BytesAdapter;

record Uncompressed() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        return bytes;
    }
}
