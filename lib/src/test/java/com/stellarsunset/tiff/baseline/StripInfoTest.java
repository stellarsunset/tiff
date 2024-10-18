package com.stellarsunset.tiff.baseline;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.RowsPerStrip;
import com.stellarsunset.tiff.baseline.tag.StripByteCounts;
import com.stellarsunset.tiff.baseline.tag.StripOffsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StripInfoTest {

    @Test
    void testFrom() {

        Ifd ifd = new Ifd(
                (short) 3,
                new Entry[]{
                        new Entry.Long(RowsPerStrip.ID, new int[]{1}),
                        new Entry.Long(StripOffsets.ID, new int[]{20, 40, -1}),
                        new Entry.Long(StripByteCounts.ID, new int[]{10, 10, -1})
                },
                0
        );

        StripInfo stripInfo = StripInfo.from(ifd);
        assertAll(
                () -> assertEquals(1L, stripInfo.rowsPerStrip(), "Rows Per Strip"),
                () -> assertArrayEquals(new long[]{20, 40, 4294967295L}, stripInfo.stripOffsets(), "Strip Offset"),
                () -> assertArrayEquals(new long[]{10, 10, 4294967295L}, stripInfo.stripByteCounts(), "Strip Byte Counts"),
                () -> assertThrows(IllegalArgumentException.class, stripInfo::asIntInfo, "Should fail on narrowing to int values.")
        );
    }
}
