package com.stellarsunset.tiff.baseline.geokey;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.extension.geokey.MissingRequiredGeoKeyException;
import com.stellarsunset.tiff.extension.geokey.ModelType;
import com.stellarsunset.tiff.extension.geokey.RasterType;
import com.stellarsunset.tiff.extension.geokey.UnsupportedTypeForGeoKeyException;
import com.stellarsunset.tiff.extension.tag.GeoKeyDirectory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelTypeTest {

    @Test
    void testWrongType() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Float(ModelType.ID, new float[]{1.0f})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertThrows(UnsupportedTypeForGeoKeyException.class, () -> ModelType.getRequired(gkd));
    }

    @Test
    void testMissingId() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(RasterType.ID, new short[]{1})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertThrows(MissingRequiredGeoKeyException.class, () -> ModelType.getRequired(gkd));
    }

    @Test
    void testCorrect() {

        Ifd.Entry[] entry = new Ifd.Entry[]{
                new Ifd.Entry.Short(ModelType.ID, new short[]{1})
        };

        GeoKeyDirectory gkd = GeoKeyDirectory.v1(entry);
        assertEquals(1, ModelType.getRequired(gkd));
    }
}
