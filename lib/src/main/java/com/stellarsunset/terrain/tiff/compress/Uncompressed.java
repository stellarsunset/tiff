package com.stellarsunset.terrain.tiff.compress;

import com.stellarsunset.terrain.tiff.BytesAdapter;

record Uncompressed() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        return new byte[0];
    }
}
