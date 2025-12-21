package io.github.stellarsunset.tiff.extension;

import com.google.common.base.Preconditions;
import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.baseline.StripInfo;
import io.github.stellarsunset.tiff.extension.tag.TileByteCounts;
import io.github.stellarsunset.tiff.extension.tag.TileLength;
import io.github.stellarsunset.tiff.extension.tag.TileOffsets;
import io.github.stellarsunset.tiff.extension.tag.TileWidth;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Container class for the related extension tags,
 * <ol>
 *     <li>{@link TileLength}</li>
 *     <li>{@link TileWidth}</li>
 *     <li>{@link TileOffsets}</li>
 *     <li>{@link TileByteCounts}</li>
 * </ol>
 *
 * <p>Analog to {@link StripInfo} + {@link ImageDimensions} but for tiled images.
 */
public record TileInfo(long length, long width, long[] offsets, long[] byteCounts) {

    public static TileInfo getRequired(Ifd ifd) {
        return new TileInfo(
                TileLength.get(ifd),
                TileWidth.get(ifd),
                TileOffsets.get(ifd),
                TileByteCounts.get(ifd)
        );
    }

    public static Optional<TileInfo> getOptional(Ifd ifd) {

        OptionalLong length = TileLength.getIfPresent(ifd);
        OptionalLong width = TileWidth.getIfPresent(ifd);
        Optional<long[]> offsets = TileOffsets.getIfPresent(ifd);
        Optional<long[]> byteCounts = TileByteCounts.getIfPresent(ifd);

        if (length.isPresent() && width.isPresent() && offsets.isPresent() && byteCounts.isPresent()) {
            return Optional.of(
                    new TileInfo(
                            length.getAsLong(),
                            width.getAsLong(),
                            offsets.get(),
                            byteCounts.get()
                    )
            );
        }
        return Optional.empty();
    }

    public Int asIntInfo() {

        Preconditions.checkArgument(length < Integer.MAX_VALUE,
                "TileLength should be less than Integer.MAX_VALUE, value was %s", length);

        Preconditions.checkArgument(width < Integer.MAX_VALUE,
                "TileWidth should be less than Integer.MAX_VALUE, value was %s", width);

        int[] intByteCounts = new int[byteCounts.length];
        for (int i = 0; i < byteCounts.length; i++) {

            long tileByteCount = byteCounts[i];

            Preconditions.checkArgument(tileByteCount < Integer.MAX_VALUE,
                    "TileByteCount should be less than Integer.MAX_VALUE, value was %s for tile %s", tileByteCount, i);

            intByteCounts[i] = (int) tileByteCount;
        }

        return new Int((int) length, (int) width, offsets, intByteCounts);
    }

    /**
     * See {@link StripInfo.Int}.
     */
    public record Int(int length, int width, long[] offsets, int[] byteCounts) {
    }
}
