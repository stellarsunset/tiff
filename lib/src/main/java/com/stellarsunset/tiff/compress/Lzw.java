package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.BytesAdapter;

record Lzw() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        return bytes;
    }
}