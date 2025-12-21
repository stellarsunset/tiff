package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.Optional;

/**
 * For each tile, the byte offset of that tile, as compressed and stored on disk. The offset is specified with respect
 * to the beginning of the TIFF file. Note that this implies that each tile has a location independent of the locations
 * of other tiles.
 *
 * <p>Offsets are ordered left-to-right and top-to-bottom. For PlanarConfiguration = 2, the offsets for the first component
 * plane are stored first, followed by all the offsets for the second component plane, and so on.
 *
 * <p>No default. See also {@link TileLength}, {@link TileOffsets}, {@link TileByteCounts}.
 */
public final class TileOffsets implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x144, "TILE_OFFSETS");

    public static long[] get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<long[]> getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUIntArray(TAG, ifd);
    }
}
