package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectedCrsTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(ProjectedCrs.ID, new float[]{1.0f})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertThrows(UnsupportedTypeForGeoKeyException.class, () -> ProjectedCrs.getRequired(gkd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(GeodeticCrs.ID, new short[]{1})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertThrows(MissingRequiredGeoKeyException.class, () -> ProjectedCrs.getRequired(gkd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(ProjectedCrs.ID, new short[]{1})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertEquals(1, ProjectedCrs.getRequired(gkd));
    }
}
