package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.BytesAdapter;

record Uncompressed() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        return new byte[0];
    }
}
