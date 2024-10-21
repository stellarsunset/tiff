# Tiff

[![Test](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml/badge.svg)](https://github.com/stellarsunset/tiff/actions/workflows/test.yaml)
[![codecov](https://codecov.io/gh/stellarsunset/tiff/graph/badge.svg?token=2SZ6MJxyXA)](https://codecov.io/gh/stellarsunset/tiff)

A data-driven library for interacting with common TIFF image types.

## Motivation

Take advantage of the newer JDK features in 21/23 to build a data-oriented programming model for interacting with TIFF
files.

```java
TiffFile file = TiffFileReader.read(FileChannel.open(FILE.toPath()));

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

## Notes

1. This repo is published to maven central as `io.github.stellarsunset:tiff`, see releases for versions
2. This implementation relied primarily on TIFF6
   documentation [here](https://www.itu.int/itudoc/itu-t/com16/tiff-fx/docs/tiff6.pdf)
3. Example `.tiff` images were taken from [here](https://people.math.sc.edu/Burkardt/data/tif/tif.html)
   and [here](https://github.com/tlnagy/exampletiffs/tree/master)
4. Implementations are regression tested against the [NGA TIFF](https://github.com/ngageoint/tiff-java) library