package com.stellarsunset.terrain.tiff.compress;

import com.stellarsunset.terrain.tiff.BytesAdapter;

record Lzw() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        return bytes;
    }
}
