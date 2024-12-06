package com.stellarsunset.tiff.baseline;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ImageLoadBenchmarks {

    @Benchmark
    @Fork(value = 1)
    public void baselineGrayscale() throws IOException {
        ResourceImageReader.loadImage("baseline/grayscale.tif", 0);
    }

    @Benchmark
    @Fork(value = 1)
    public void ngaBaselineGrayscale() throws IOException {
        NgaImageReader.loadByteImage("baseline/grayscale.tif", 0);
    }

    @Benchmark
    @Fork(value = 1)
    public void baselinePalette() throws IOException {
        ResourceImageReader.loadImage("baseline/palette.tif", 0);
    }

    @Benchmark
    @Fork(value = 1)
    public void ngaBaselinePalette() throws IOException {
        NgaImageReader.loadByteImage("baseline/palette.tif", 0);
    }

    @Benchmark
    @Fork(value = 1)
    public void baselineRgb() throws IOException {
        ResourceImageReader.loadImage("baseline/rgb.tif", 0);
    }

    @Benchmark
    @Fork(value = 1)
    public void ngaBaselineRgb() throws IOException {
        NgaImageReader.loadByteImage("baseline/rgb.tif", 0);
    }
}
