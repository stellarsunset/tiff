package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.extension.tag.TileByteCounts;
import io.github.stellarsunset.tiff.extension.tag.TileLength;
import io.github.stellarsunset.tiff.extension.tag.TileOffsets;
import io.github.stellarsunset.tiff.extension.tag.TileWidth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TileInfoTest {

    @Test
    void testGetRequired() {

        Ifd ifd = new Ifd(
                (short) 4,
                new Entry[]{
                        new Entry.Long(TileLength.TAG.id(), new int[]{1}),
                        new Entry.Long(TileWidth.TAG.id(), new int[]{1}),
                        new Entry.Long(TileOffsets.TAG.id(), new int[]{20, 40, -1}),
                        new Entry.Long(TileByteCounts.TAG.id(), new int[]{10, 10, -1})
                },
                0
        );

        TileInfo tileInfo = TileInfo.getRequired(ifd);
        assertAll(
                () -> assertEquals(1L, tileInfo.length(), "Tile Length"),
                () -> assertEquals(1L, tileInfo.width(), "Tile Width"),
                () -> assertArrayEquals(new long[]{20, 40, 4294967295L}, tileInfo.offsets(), "Tile Offsets"),
                () -> assertArrayEquals(new long[]{10, 10, 4294967295L}, tileInfo.byteCounts(), "Tile Byte Counts"),
                () -> assertThrows(IllegalArgumentException.class, tileInfo::asIntInfo, "Should fail on narrowing to int values.")
        );
    }
}
