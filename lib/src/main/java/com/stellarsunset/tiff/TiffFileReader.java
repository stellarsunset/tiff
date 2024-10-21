package com.stellarsunset.tiff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public final class TiffFileReader {

    private static final short LE = 0x4949;

    private static final short BE = 0x4D4D;

    private final Image.Maker imageMaker;

    private TiffFileReader(Image.Maker imageMaker) {
        this.imageMaker = requireNonNull(imageMaker);
    }

    public static TiffFileReader baseline() {
        return new TiffFileReader(Image.Maker.baseline());
    }

    public static TiffFileReader withMaker(Image.Maker maker) {
        return new TiffFileReader(maker);
    }

    /**
     * Read the contents of the provided {@link SeekableByteChannel} as a TIFF file.
     *
     * <p>Its typical to provision the {@link SeekableByteChannel} via {@link FileChannel#open(Path, OpenOption...)}.
     *
     * <p>This byte channel will be used to lazily load image data associated with the TIFF file, so clients should think
     * carefully about how they provision this channel before handing it off (e.g. memory mapped).
     *
     * @param channel the {@link SeekableByteChannel} pointing to the contents of the TIFF file
     */
    public TiffFile read(SeekableByteChannel channel) {
        try {

            TiffHeader header = readHeader(channel);

            BytesAdapter adapter = BytesAdapter.of(header.order());

            IfdReader ifdReader = new IfdReader(adapter);

            Ifd first = ifdReader
                    .read(channel, header.unsignedFirstIfdOffset());

            List<Ifd> ifds = new ArrayList<>();
            List<Image> images = new ArrayList<>();

            Ifd ifd = first;
            while (ifd.unsignedNextIfdOffset() != 0) {
                ifds.add(ifd);
                images.add(imageMaker.makeImage(channel, header.order(), ifd));

                ifd = ifdReader.read(channel, ifd.unsignedNextIfdOffset());
            }

            ifds.add(ifd);
            images.add(imageMaker.makeImage(channel, header.order(), ifd));

            return new TiffFile(
                    channel,
                    header,
                    ifds.toArray(new Ifd[0]),
                    images.toArray(new Image[0])
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to open connection to or read the contents of the provided TIFF file.", e
            );
        }
    }

    private static TiffHeader readHeader(SeekableByteChannel channel) throws IOException {
        channel.position(0);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        channel.read(buffer);

        short orderBytes = buffer.getShort(0);

        ByteOrder order = switch (orderBytes) {
            case LE -> ByteOrder.LITTLE_ENDIAN;
            case BE -> ByteOrder.BIG_ENDIAN;
            default -> throw new IllegalArgumentException("Unknown ByteOrder for TIFF: " + orderBytes);
        };

        buffer.order(order);

        return new TiffHeader(
                order,
                buffer.getShort(2),
                buffer.getInt(4)
        );
    }
}
