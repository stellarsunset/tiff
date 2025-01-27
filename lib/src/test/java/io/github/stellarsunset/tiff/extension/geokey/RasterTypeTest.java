package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RasterTypeTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(RasterType.ID, new float[]{1.0f})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertThrows(UnsupportedTypeForGeoKeyException.class, () -> RasterType.getRequired(gkd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(ModelType.ID, new short[]{1})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertThrows(MissingRequiredGeoKeyException.class, () -> RasterType.getRequired(gkd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(RasterType.ID, new short[]{1})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertEquals(1, RasterType.getRequired(gkd));
    }
}
