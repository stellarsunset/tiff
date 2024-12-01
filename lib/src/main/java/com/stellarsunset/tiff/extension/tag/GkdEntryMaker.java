package com.stellarsunset.tiff.extension.tag;

import com.stellarsunset.tiff.Ifd;

import java.util.Arrays;

/**
 * Similar in nature to the {@code IfdEntryMaker} but for materializing the tag values associated with GeoKey entries
 * in a GKD ({@link GeoKeyDirectory}).
 */
record GkdEntryMaker(Ifd ifd) {

    /**
     * Make the fully inflated entry values for all GeoKey entries pulled from the IDF.
     *
     * @param geoKeyEntries the {@link short[]} of entries pulled from the GKD
     * @param offset        the startByte offset in the array (always 4)
     */
    public Ifd.Entry[] makeAllEntries(short[] geoKeyEntries, int offset) {

        int n = (geoKeyEntries.length - offset) / 4;
        Ifd.Entry[] entries = new Ifd.Entry[n];

        for (int i = 0; i < n; i++) {
            int o = offset + (i * 4);
            entries[i] = makeEntry(
                    geoKeyEntries[o],
                    geoKeyEntries[o + 1],
                    geoKeyEntries[o + 2],
                    geoKeyEntries[o + 3]
            );
        }

        Arrays.sort(entries);
        return entries;
    }

    /**
     * Create a new {@link Ifd.Entry} with the provided GeoKey key ID and with a count of values stored at the provided
     * offset in the referenced TIFF tag.
     *
     * @param key         the GeoKey ID, i.e. the "tag" ID of the GeoKey
     * @param tag         the identifier of the TIFF tag containing the key data (if relevant)
     * @param count       the number of entries associated with the key
     * @param valueOffset the offset in the underlying tag data array after which to collect values
     */
    public Ifd.Entry makeEntry(short key, short tag, short count, short valueOffset) {

        if (tag == 0) {
            return new Ifd.Entry.Short(key, new short[]{valueOffset});
        }

        Ifd.Entry storage = ifd.findTag(tag);
        int offset = Short.toUnsignedInt(valueOffset);

        return copyOfRange(key, storage, offset, offset + Short.toUnsignedInt(count));
    }

    private Ifd.Entry copyOfRange(short key, Ifd.Entry tag, int from, int to) {
        return switch (tag) {
            case Ifd.Entry.Ascii ascii -> new Ifd.Entry.Ascii(
                    key,
                    Arrays.copyOfRange(ascii.values(), from, to)
            );
            case Ifd.Entry.Byte aByte -> new Ifd.Entry.Byte(
                    key,
                    Arrays.copyOfRange(aByte.values(), from, to)
            );
            case Ifd.Entry.Double aDouble -> new Ifd.Entry.Double(
                    key,
                    Arrays.copyOfRange(aDouble.values(), from, to)
            );
            case Ifd.Entry.Float aFloat -> new Ifd.Entry.Float(
                    key,
                    Arrays.copyOfRange(aFloat.values(), from, to)
            );
            case Ifd.Entry.Long aLong -> new Ifd.Entry.Long(
                    key,
                    Arrays.copyOfRange(aLong.values(), from, to)
            );
            case Ifd.Entry.NotFound notFound -> new Ifd.Entry.NotFound(key);
            case Ifd.Entry.Rational rational -> new Ifd.Entry.Rational(
                    key,
                    Arrays.copyOfRange(rational.numerators(), from, to),
                    Arrays.copyOfRange(rational.denominators(), from, to)
            );
            case Ifd.Entry.SByte sByte -> new Ifd.Entry.SByte(
                    key,
                    Arrays.copyOfRange(sByte.values(), from, to)
            );
            case Ifd.Entry.SLong sLong -> new Ifd.Entry.SLong(
                    key,
                    Arrays.copyOfRange(sLong.values(), from, to)
            );
            case Ifd.Entry.SRational sRational -> new Ifd.Entry.SRational(
                    key,
                    Arrays.copyOfRange(sRational.numerators(), from, to),
                    Arrays.copyOfRange(sRational.denominators(), from, to)
            );
            case Ifd.Entry.SShort sShort -> new Ifd.Entry.SShort(
                    key,
                    Arrays.copyOfRange(sShort.values(), from, to)
            );
            case Ifd.Entry.Short aShort -> new Ifd.Entry.Short(
                    key,
                    Arrays.copyOfRange(aShort.values(), from, to)
            );
            case Ifd.Entry.Undefined undefined -> new Ifd.Entry.Undefined(
                    key,
                    Arrays.copyOfRange(undefined.values(), from, to)
            );
        };
    }
}
