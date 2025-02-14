module tiff.lib.main {
    requires com.google.common;
    requires org.checkerframework.checker.qual;
    requires java.desktop;

    exports io.github.stellarsunset.tiff;
    exports io.github.stellarsunset.tiff.compress;

    exports io.github.stellarsunset.tiff.baseline;
    exports io.github.stellarsunset.tiff.baseline.tag;

    exports io.github.stellarsunset.tiff.extension;
    exports io.github.stellarsunset.tiff.extension.tag;
}