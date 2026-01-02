# Tiff

[![Test](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml/badge.svg)](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml)
[![codecov](https://codecov.io/gh/stellarsunset/tiff/graph/badge.svg?token=2SZ6MJxyXA)](https://codecov.io/gh/stellarsunset/tiff)

A data-driven library for interacting with common TIFF image types.

## Motivation

Take advantage of the newer JDK features in 21/23 to build a data-oriented programming model for interacting with TIFF
files.

Our expectation is that most clients will be working with a small number of concrete image types in their applications
(usually 1), so this library provides:

1. Generic tools to explore unknown image data through models that naturally reflect TIFFs organization
2. Concrete, intuitive, image handles for production/regular use

```java
TiffFile file = TiffFileReader.withMaker(BaselineImage.maker())
        .read(FileChannel.open(FILE.toPath()));

// the IFD for the first image in the TIFF file
Ifd ifd0 = file.ifd(0);

// static utility classes are provided for tag-value access, values types are handled via 
// enhanced switch, unsigned entry values are returned up-cast (e.g. ushort -> int)
int xResolution = XResolution.get(ifd0);

// the first image in the file, images can either be Baseline or Extension types
Image image0 = file.image(0);

// most image handles are lazy by default to prevent eager loading of raster data, allowing 
// inspection of IFD entries before deciding to load
public Optional<RgbImage> asRgb(Image image) {
    if (image instanceof Image.Lazy l) {
        return asRgb(l);
    }
    return image instanceof RgbImage r ? Optional.of(r) : Optional.empty();
}

// BaselineImage subclasses provide richer Pixel types with more semantic information
RgbImage rgb0 = asRgb(image0).orElseThrow();

RgbImage.Pixel rgb0_0 = rgb0.valueAt(0, 0);
int r = rgb0_0.unsignedR();
int g = rgb0_0.unsignedG();
int b = rgb0_0.unsignedB();
```

## Extensions

The strength of the TIFF specification is its extensibility, meaning there is a rich ecosystem of non-baseline
image types and encoding methods that are widely used.

This library out-of-the-box supports a subset of these extensions including:

1. Tiling (for all `Image` subtypes defined in-repo)
2. LZW compression
3. Differencing predictors
4. More... see following sections

### "Data" Images

The baseline TIFF spec standardizes displaying and formatting for a "baseline" set of images, however TIFF image rasters
(the array of pixels) can also be used to carry through data in the more traditional sense.

A good example is GeoTIFF where image data is often 32-bit floating-point elevation data. Built-in to the library are a
small number of `DataImage` types for handling these more generic rasters.

```java
TiffFile file = TiffFileReader.withMaker(DataImage.maker())
        .read(FileChannel.open(FILE.toPath()));

Image image0 = file.image(0);

// Access to these is identical to baseline images, in this case were looking for an image 
// with 32-bit floating-point raster data with three samples/components per pixel
public Optional<Float3Image> asFloat(Image image) {
    if (image instanceof Image.Lazy l) {
        return asRgb(l);
    }
    return image instanceof Float3Image f ? Optional.of(f) : Optional.empty();
}

// DataImage subclasses provided well-typed access to image data (rather than using boxed 
// objects like Number[])
Float3Image float0 = asFloat(image0);

Float3Image.Pixel float0_0 = float0.valueAt(0, 0);
float f1 = float0_0.f1();
float f2 = float0_0.f2();
float f3 = float0_0.f3();
```

### GeoTIFF

The GeoTIFF extension allows clients to embed a rich set of geospatial tags used to geo-reference image data within a
TIFF file using a specialized tag called a GeoKey Directory (GKD).

Functionally this GKD is identical to encoding another Image File Directory (IFD) as a tag inside a TIFF IFD (we've gone
meta). Access the GKD through normal tag syntax:

```java
GeoKeyDirectory gkd = GeoKeyDirectory.get(ifd);

// interact with the GKD like an IFD
int rasterType = RasterType.get(gkd);
int modelType = ModelType.get(gkd);
```

GeoKeys allow clients to geo-reference TIFF raster data, i.e. put pixels on a map and images are used to indicate land
cover
(e.g. Ice vs Open Water vs etc. in a Palette-Color image) or may encode data such as elevation in the raster.

This library purposefully doesn't include a coordinate transform system so clients can pick one that suits their needs
without dependency conflicts.

## Notes

1. This repo is published to maven central as `io.github.stellarsunset:tiff`, see releases for versions
2. This implementation relied primarily on TIFF6
   documentation [here](https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf)
3. Example `.tiff` images were taken from [here](https://people.math.sc.edu/Burkardt/data/tif/tif.html)
   and [here](https://github.com/tlnagy/exampletiffs/tree/master)
4. Implementations are regression tested against the [NGA TIFF](https://github.com/ngageoint/tiff-java) library
5. Example [GeoTIFF files](https://prd-tnm.s3.amazonaws.com/index.html?prefix=StagedProducts/Elevation/) published by
   USGS
6. To explore TIFF files the `tiffinfo` cli tool is a great resource

## TODO

1. Fixup tags
    1. Add some way to find all current implementations
    2. Add some way to go from code -> human-readable names
2. Modified Huffman compression for BiLevel images
2. Write files?
