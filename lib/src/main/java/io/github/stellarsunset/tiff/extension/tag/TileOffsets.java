package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

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
public final class TileOffsets {

    public static final String NAME = "TILE_OFFSETS";

    public static final short ID = 0x144;

    public static long[] getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<long[]> getOptional(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Optional.of(Arrays.toUnsignedLongArray(s.values()));
            case Entry.Long l -> Optional.of(Arrays.toUnsignedLongArray(l.values()));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _,
                 Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
