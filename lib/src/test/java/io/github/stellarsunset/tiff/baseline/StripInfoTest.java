package io.github.stellarsunset.tiff.baseline;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.RowsPerStrip;
import io.github.stellarsunset.tiff.baseline.tag.StripByteCounts;
import io.github.stellarsunset.tiff.baseline.tag.StripOffsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StripInfoTest {

    @Test
    void testGetRequired() {

        Ifd ifd = new Ifd(
                (short) 3,
                new Entry[]{
                        new Entry.Long(RowsPerStrip.TAG.id(), new int[]{1}),
                        new Entry.Long(StripOffsets.TAG.id(), new int[]{20, 40, -1}),
                        new Entry.Long(StripByteCounts.TAG.id(), new int[]{10, 10, -1})
                },
                0
        );

        StripInfo stripInfo = StripInfo.getRequired(ifd);
        assertAll(
                () -> assertEquals(1L, stripInfo.rowsPerStrip(), "Rows Per Strip"),
                () -> assertArrayEquals(new long[]{20, 40, 4294967295L}, stripInfo.stripOffsets(), "Strip Offset"),
                () -> assertArrayEquals(new long[]{10, 10, 4294967295L}, stripInfo.stripByteCounts(), "Strip Byte Counts"),
                () -> assertThrows(IllegalArgumentException.class, stripInfo::asIntInfo, "Should fail on narrowing to int values.")
        );
    }

    @Test
    void testAsIntInfo() {
        Ifd ifd = new Ifd(
                (short) 3,
                new Entry[]{
                        new Entry.Long(RowsPerStrip.TAG.id(), new int[]{1}),
                        new Entry.Long(StripOffsets.TAG.id(), new int[]{20, 40, 1}),
                        new Entry.Long(StripByteCounts.TAG.id(), new int[]{10, 10, 1})
                },
                0
        );

        StripInfo.Int stripInfo = StripInfo.getRequired(ifd).asIntInfo();
        assertAll(
                () -> assertEquals(1L, stripInfo.rowsPerStrip(), "Rows Per Strip"),
                () -> assertArrayEquals(new long[]{20, 40, 1}, stripInfo.stripOffsets(), "Strip Offset"),
                () -> assertArrayEquals(new int[]{10, 10, 1}, stripInfo.stripByteCounts(), "Strip Byte Counts")
        );
    }
}
