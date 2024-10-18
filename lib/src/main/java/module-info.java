module tiff.lib.main {
    requires com.google.common;

    exports com.stellarsunset.tiff;
    exports com.stellarsunset.tiff.compress;

    exports com.stellarsunset.tiff.baseline;
    exports com.stellarsunset.tiff.baseline.tag;

    exports com.stellarsunset.tiff.extension;
}