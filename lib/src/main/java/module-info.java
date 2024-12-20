module tiff.lib.main {
    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires java.desktop;

    exports com.stellarsunset.tiff;
    exports com.stellarsunset.tiff.compress;

    exports com.stellarsunset.tiff.baseline;
    exports com.stellarsunset.tiff.baseline.tag;

    exports com.stellarsunset.tiff.extension;
    exports com.stellarsunset.tiff.extension.tag;
}