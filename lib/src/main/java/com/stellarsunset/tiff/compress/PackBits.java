package com.stellarsunset.tiff.compress;

import com.stellarsunset.tiff.BytesAdapter;

import java.io.ByteArrayOutputStream;

import static java.util.Objects.requireNonNull;

/**
 * See <a href="https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf">Section 9</a> for a description of the PackBits
 * compression algorithm.
 */
record PackBits() implements Compressor {
    @Override
    public byte[] decompress(byte[] bytes, BytesAdapter adapter) {

        Reader reader = new Reader(bytes, adapter);
        ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();

        while (reader.hasByte()) {
            int header = reader.readByte();
            if (header != -128) {
                if (0 <= header) {
                    for (int i = 0; i <= header; i++) {
                        decodedStream.write(reader.readByte());
                    }
                } else {
                    int next = reader.readByte();
                    for (int i = 0; i <= -header; i++) {
                        decodedStream.write(next);
                    }
                }
            }
        }

        return decodedStream.toByteArray();
    }

    private static final class Reader {

        private final byte[] bytes;

        private final BytesAdapter adapter;

        private int offset = 0;

        private Reader(byte[] bytes, BytesAdapter adapter) {
            this.bytes = requireNonNull(bytes);
            this.adapter = requireNonNull(adapter);
        }

        boolean hasByte() {
            return offset < bytes.length;
        }

        byte readByte() {
            byte b = adapter.adaptRawByte(bytes[offset]);
            offset++;
            return b;
        }
    }
}
