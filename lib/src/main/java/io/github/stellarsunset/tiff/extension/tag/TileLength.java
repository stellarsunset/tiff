package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;

import java.util.OptionalLong;

/**
 * The tile length (height) in pixels. This is the number of rows in each tile.
 *
 * <p>TileLength must be a multiple of 16 for compatibility with compression schemes such as JPEG.
 *
 * <p>Replaces RowsPerStrip in tiled TIFF files.
 *
 * <p>No default. See also {@link TileLength}, {@link TileOffsets}, {@link TileByteCounts}.
 */
public final class TileLength implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x143, "TILE_LENGTH");

    public static long get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static OptionalLong getIfPresent(Ifd ifd) {
        return Tag.Accessor.optionalUInt(TAG, ifd);
    }
}
