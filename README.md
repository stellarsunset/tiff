# Tiff

[![Test](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml/badge.svg)](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml)
[![codecov](https://codecov.io/gh/stellarsunset/tiff/graph/badge.svg?token=2SZ6MJxyXA)](https://codecov.io/gh/stellarsunset/tiff)

A data-driven library for interacting with common tiff image formats.

## Motivation

We want to take advantage for the newer JDK features in 21/23 to build a data-oriented programming model for interacting
with TIFF files.

```java
TiffFile file = TiffFileReader.read(FileChannel.open(FILE.toPath()));

// the IFD for the first image in the TIFF file
Ifd ifd0 = file.ifd(0);

// static utility classes are provided for tag-value access, values types are handled via 
// enhanced switch, unsigned entry values are returned up-cast (e.g. ushort -> int)
int xResolution = XResolution.getRequired(ifd0);

// the first image in the file, images can either be Baseline or Extension types
Image image0 = file.image(0);

/** Handle the class sealed interface hierarchy to return a concrete image types. */
public RgbImage asRgb(Image image) {
    return switch (image) {
        case Image.Lazy l -> asRgb(l.delegate());
        case Baseline b -> switch (b) {
            case RgbImage r -> r;
            case BiLevelImage _, GrayscaleImage _, PaletteColorImage _ -> throw IllegalArgumentException();
        };
        case Extension e -> throw IllegalArgumentException();
    };
}

// convert to a baseline TIFF spec RGB image
RgbImage rgb0 = asRgb(image0);

// work with their concrete PixelValue types
PixelValue.Rgb rgb0_0 = rgb0.valueAt(0, 0);
int r = rgb0_0.unsignedR();
int g = rgb0_0.unsignedG();
int b = rgb0_0.unsignedB();
```

## Useful links

Example `.tiff` images were taken from [here](https://people.math.sc.edu/Burkardt/data/tif/tif.html)
and [here](https://github.com/tlnagy/exampletiffs/tree/master).

This implementation relied primarily on TIFF6
documentation [here](https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf).