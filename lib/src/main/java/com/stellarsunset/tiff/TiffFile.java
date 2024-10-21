package com.stellarsunset.tiff;

import java.nio.channels.SeekableByteChannel;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Handle for a TIFF file, created via the {@link TiffFileReader}.
 *
 * <p>{@link TiffFile}s are {@link AutoCloseable} because they may hold onto an open file pointer to lazily read bytes
 * from the underlying file.
 */
public record TiffFile(SeekableByteChannel channel, TiffHeader header, Ifd[] ifds,
                       Image[] images) implements AutoCloseable {

    public TiffFile {
        checkArgument(ifds.length == images.length, "Should be as many IFDs as Images.");
        checkArgument(ifds.length > 0, "Should be at least one IFD/Image in the file.");
    }

    /**
     * Convenience, returns the nth {@link Ifd} in the file, there is always at least one.
     */
    public Ifd ifd(int n) {
        return ifds[n];
    }

    /**
     * Convenience, returns the image associated with the nth {@link Ifd} in the file, there is always at least one.
     */
    public Image image(int n) {
        return images[n];
    }

    /**
     * Convenience, returns the total number of {@link Image}s/{@link Ifd}s in the TIFF file.
     *
     * <p>When querying specific images via {@link #image(int)}, the allowed values are {@code [0, numberOfImages-1]}.
     */
    public int numberOfImages() {
        return ifds.length;
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }
}
