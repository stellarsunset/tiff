package com.stellarsunset.terrain.tiff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public record BytesReader(SeekableByteChannel channel) {

    public ByteBuffer readBytes(long position, int bytesToRead) {
        try {
            channel.position(position);
            ByteBuffer bytes = ByteBuffer.allocate(bytesToRead);
            channel.read(bytes);
            return bytes.position(0); // move back to offset 0
        } catch (IOException e) {
            String message = String.format("Unable to read %d bytes at position %d in file.", bytesToRead, position);
            throw new IllegalArgumentException(message, e);
        }
    }
}
