package io.github.stellarsunset.tiff.extension.geokey;

/**
 * Keep package private to keep off client classpath.
 */
final class Arrays {

    private Arrays() {
    }

    static int[] toUnsignedIntArray(short[] shorts) {
        int[] ints = new int[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            ints[i] = Short.toUnsignedInt(shorts[i]);
        }
        return ints;
    }

    static long[] toUnsignedLongArray(short[] shorts) {
        long[] longs = new long[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            longs[i] = Short.toUnsignedLong(shorts[i]);
        }
        return longs;
    }

    static long[] toUnsignedLongArray(int[] ints) {
        long[] longs = new long[ints.length];
        for (int i = 0; i < ints.length; i++) {
            longs[i] = Integer.toUnsignedLong(ints[i]);
        }
        return longs;
    }
}
