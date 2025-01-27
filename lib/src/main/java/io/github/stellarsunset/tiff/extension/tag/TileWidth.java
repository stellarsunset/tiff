package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.ImageLength;
import io.github.stellarsunset.tiff.baseline.tag.ImageWidth;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.util.OptionalLong;

/**
 * The tile width in pixels. This is the number of columns in each tile.
 *
 * <p>Assuming integer arithmetic, three computed values that are useful in the following field descriptions are:
 * <pre>
 *     TilesAcross = (ImageWidth + TileWidth - 1) / TileWidth
 *     TilesPerImage = TilesAcross * TilesDown
 *     TilesDown = (ImageLength + TileLength - 1) / TileLength
 * </pre>
 * <p>These computed values are not TIFF fields; they are simply values determined by the:
 * <ol>
 *     <li>{@link ImageWidth}</li>
 *     <li>{@link TileWidth}</li>
 *     <li>{@link ImageLength}</li>
 *     <li>{@link TileLength}</li>
 * </ol>
 * <p>TileWidth and ImageWidth together determine the number of tiles that span the width of the image (TilesAcross).
 * TileLength and ImageLength together determine the number of tiles that span the length of the image (TilesDown).
 *
 * <p>We recommend choosing TileWidth and TileLength such that the resulting tiles are about 4K to 32K bytes before
 * compression. This seems to be a reasonable value for most applications and compression schemes.
 *
 * <p>TileWidth must be a multiple of 16. This restriction improves performance in some graphics environments and enhances
 * compatibility with compression schemes such as JPEG.
 *
 * <p>Tiles need not be square.
 *
 * <p>Note that ImageWidth can be less than TileWidth, although this means that the tiles are too large or that you are
 * using tiling on really small images, neither of which is recommended. The same observation holds for ImageLength and
 * TileLength.
 *
 * <p>No default. See also {@link TileLength}, {@link TileOffsets}, {@link TileByteCounts}.
 */
public final class TileWidth {

    public static final String NAME = "TILE_WIDTH";

    public static final short ID = 0x142;

    public static long getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static OptionalLong getOptional(Ifd ifd) {
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
