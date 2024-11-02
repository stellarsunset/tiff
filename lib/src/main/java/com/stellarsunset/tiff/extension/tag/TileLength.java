package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

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
public final class TileLength {

    public static final String NAME = "TILE_LENGTH";

    public static final short ID = 0x143;

    public static long getRequired(Ifd ifd) {
        return getOptionalValue(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static OptionalLong getOptionalValue(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> OptionalLong.of(Short.toUnsignedLong(s.values()[0]));
            case Entry.Long l -> OptionalLong.of(Integer.toUnsignedLong(l.values()[0]));
            case Entry.NotFound _ -> OptionalLong.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _,
                 Entry.SLong _, Entry.SRational _,
                 Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }
}
