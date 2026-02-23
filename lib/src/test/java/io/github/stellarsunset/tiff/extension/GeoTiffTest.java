package io.github.stellarsunset.tiff.extension;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Image;
import io.github.stellarsunset.tiff.TiffFile;
import io.github.stellarsunset.tiff.TiffFileReader;
import io.github.stellarsunset.tiff.baseline.ImageDimensions;
import io.github.stellarsunset.tiff.baseline.RasterHelpers;
import io.github.stellarsunset.tiff.extension.geokey.GeodeticCrs;
import io.github.stellarsunset.tiff.extension.geokey.ModelType;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TiffReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static org.junit.jupiter.api.Assertions.*;

class GeoTiffTest {

    private static final File FILE = tiffFile("extension/geotiff/usgs.tif");

    @Test
    void test() {
        try (TiffFile file = TiffFileReader.withMaker(DataImage.maker()).read(FileChannel.open(FILE.toPath()))) {

            Ifd ifd = file.ifd(0);

            GeoKeyDirectory gkd = GeoKeyDirectory.get(ifd);
            assertAll(
                    () -> assertEquals(4269, GeodeticCrs.get(gkd), "GeodeticCRS"),
                    () -> assertEquals(2, ModelType.get(gkd), "ModelType")
            );

            Image image = file.image(0);
            assertInstanceOf(FloatImage.Float1Image.class, unwrap(image), "Should be an image of floats.");
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @Disabled("Large file not committed.")
    void testBig() {
        File big = tiffFile("extension/geotiff/big.tif");
        try (TiffFile file = TiffFileReader.withMaker(DataImage.maker()).read(FileChannel.open(big.toPath()))) {

            //ImageDimensions dims = ImageDimensions.get(file.ifd(0));
            FloatImage.Float1Image image = (FloatImage.Float1Image) unwrap(file.image(0));

            Rasters rasters = readRasters(big);
            //float[][] raster = RasterHelpers.toFloatRaster(rasters);
            //assertEquals(image.data().length, raster.length);
        } catch (Exception e) {
            fail(e);
        }
    }

    private Rasters readRasters(File file) throws IOException {
        return TiffReader.readTiff(file).getFileDirectory().readRasters();
    }

    private Image unwrap(Image image) {
        return image instanceof Image.Lazy l ? unwrap(l.delegate()) : image;
    }

    private static File tiffFile(String name) {
        return new File(System.getProperty("user.dir") + "/src/test/resources/" + name);
    }
}
