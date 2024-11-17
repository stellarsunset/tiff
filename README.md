# Tiff

[![Test](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml/badge.svg)](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml)
[![codecov](https://codecov.io/gh/stellarsunset/tiff/graph/badge.svg?token=2SZ6MJxyXA)](https://codecov.io/gh/stellarsunset/tiff)

A data-driven library for interacting with common TIFF image types.

## Motivation

Take advantage of the newer JDK features in 21/23 to build a data-oriented programming model for interacting with TIFF
files.

Our expectation is that most clients will be working with a small number of concrete image types in their applications
(usually 1), provide them a library with:

1. Generic tools to explore unknown image data
2. Code that naturally reflects the TIFF standard and image hierarchy
3. Concrete, intuitive, image handles for production/regular use

```java
TiffFile file = TiffFileReader.baseline()
        .read(FileChannel.open(FILE.toPath()));

// the IFD for the first image in the TIFF file
Ifd ifd0 = file.ifd(0);

// static utility classes are provided for tag-value access, values types are handled via 
// enhanced switch, unsigned entry values are returned up-cast (e.g. ushort -> int)
int xResolution = XResolution.getRequired(ifd0);

// the first image in the file, images can either be Baseline or Extension types
Image image0 = file.image(0);

// navigate the sealed hierarchy to safely work with concrete subtypes
public Optional<RgbImage> asRgb(Image image) {
    return switch (image) {
        case Image.Lazy lazy -> asRgb(lazy.delegate());
        case BaselineImage baselineImage -> {
            switch (baselineImage) {
                case BiLevelImage _, GrayscaleImage _, PaletteColorImage _ -> Optional.empty();
                case RgbImage rgbImage -> Optional.of(rgbImage);
            }
        }
        case Image.Unknown _, ExtensionImage _ -> Optional.empty();
    };
}

RgbImage rgb0 = asRgb(image0).orElseThrow();

// and their concrete Pixel types
Pixel.Rgb rgb0_0 = rgb0.valueAt(0, 0);

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
3. Differencing predictors (for `BitsPerSample = 1`)
4. "Data" image types (`ShortImage`, `IntImage`, `FloatImage`)
5. GeoTIFF extensions, see following section

### GeoTIFF

The GeoTIFF extension allows clients to embed a rich set of geospatial tags used to geo-reference image data within a 
TIFF file using a specialized tag called a GeoKey Directory (GKD). Functionally this GKD is identical to encoding another 
Image File Directory (IFD) as a tag inside a TIFF IFD (we've gone meta).

To access entries in the GKD a built-in tag accessor is supplied for it alongside a few common GeoKey accessors:

```java
GeoKeyDirectory gkd = GeoKeyDirectory.getRequired(ifd);

// interact with the GKD like an IFD
int rasterType = RasterType.getRequired(gkd);
int modelType = ModelType.getRequired(gkd);
```

These GeoKeys geo-reference baseline TIFF images (e.g. Palette-Color or RGB) in a coordinate reference system (put them 
one a map), often indicating things like land cover (e.g. Ice vs Open Water vs etc. in a Palette-Color image).

Frequently though, TIFFs with GeoTIFF tags/keys encode "data" in the pixels of the image raster as `Short/Int/FloatImage` 
types (i.e. 16/32-bit int or 32-bit float). Often this is elevation data, these "data images" can be accessed via: 

```java
// navigate the unsealed extension type hierarchy for "data" images
public Optional<Float1Image> asFloat1(Image image) {
    return switch (image) {
        case Image.Lazy lazy -> asFloat1(lazy.delegate());
        case Image.Unknown _, BaselineImage _ -> Optional.empty();
        case ExtensionImage e -> switch (e) {
            case FloatImage f -> switch (f) {
                case Float1Image f1 -> Optional.of(f1);
                case Float3Image _, FloatNImage _ -> Optional.empty();
            };
            default -> Optional.empty();
        };
    };
}
```

This library purposefully doesn't include a coordinate transform system: 
1. To allow clients to pick one that suits their needs without conflicts
2. To keep the dependencies of this library light-weight

## Notes

1. This repo is published to maven central as `io.github.stellarsunset:tiff`, see releases for versions
2. This implementation relied primarily on TIFF6
   documentation [here](https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf)
3. Example `.tiff` images were taken from [here](https://people.math.sc.edu/Burkardt/data/tif/tif.html)
   and [here](https://github.com/tlnagy/exampletiffs/tree/master)
4. Implementations are regression tested against the [NGA TIFF](https://github.com/ngageoint/tiff-java) library

## TODO

1. Modified Huffman compression for BiLevel images
2. Write files? 