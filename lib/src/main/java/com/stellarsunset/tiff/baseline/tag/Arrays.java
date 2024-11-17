package com.stellarsunset.tiff.baseline.tag;

public final class Arrays {

    private Arrays() {
    }

    public static int[] toUnsignedIntArray(short[] shorts) {
        int[] ints = new int[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            ints[i] = Short.toUnsignedInt(shorts[i]);
        }
        return ints;
    }

    public static long[] toUnsignedLongArray(short[] shorts) {
        long[] longs = new long[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            longs[i] = Short.toUnsignedLong(shorts[i]);
        }
        return longs;
    }

    public static long[] toUnsignedLongArray(int[] ints) {
        long[] longs = new long[ints.length];
        for (int i = 0; i < ints.length; i++) {
            longs[i] = Integer.toUnsignedLong(ints[i]);
        }
        return longs;
    }
}
