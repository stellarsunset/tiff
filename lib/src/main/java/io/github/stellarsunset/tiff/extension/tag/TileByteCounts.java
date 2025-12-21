package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Optional;

/**
 * For each tile, the number of (compressed) bytes in that tile.
 *
 * <p>See {@link TileOffsets} for a description of how the byte counts are ordered.
 *
 * <p>No default. See also {@link TileLength}, {@link TileOffsets}, {@link TileByteCounts}.
 */
public final class TileByteCounts implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x145, "TILE_BYTE_COUNTS");

    public static long[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<long[]> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUIntArray(TAG, ifd);
    }
}
