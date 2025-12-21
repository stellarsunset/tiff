module tiff.lib.main {
    requires com.google.common;
    requires java.desktop;
    requires java.smartcardio;

    exports io.github.stellarsunset.tiff;
    exports io.github.stellarsunset.tiff.compress;

    exports io.github.stellarsunset.tiff.baseline;
    exports io.github.stellarsunset.tiff.baseline.tag;

    exports io.github.stellarsunset.tiff.extension;
    exports io.github.stellarsunset.tiff.extension.tag;
    exports io.github.stellarsunset.tiff.extension.geokey;
}