package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.BytesAdapter;

record ModifiedHuffman() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
