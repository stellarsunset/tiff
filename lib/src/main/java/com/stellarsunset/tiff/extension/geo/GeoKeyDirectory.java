package com.stellarsunset.tiff.extension.geo;

import com.stellarsunset.tiff.Ifd;
import com.stellarsunset.tiff.Ifd.Entry;
import com.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import com.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;

import java.util.Arrays;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link GeoKeyDirectory} is a sort of meta-{@link Ifd} containing geospatial keys (functionally identical to TIFF
 * tags) stored across a collection of standard TIFF tags inside a normal TIFF IFD.
 *
 * <p>Clients should think about {@link GeoKeyDirectory}s as:
 * <ol>
 *     <li>Accessed like a tag, e.g. {@code GeoKeyDirectory.getOptional(ifd)}</li>
 *     <li>Used like an {@link Ifd}, e.g. {@code SomeGeoTag.getOptional(gkd)}</li>
 * </ol>
 *
 * <p>This design minimizes the number of reserved tags that need to be carved out of the underlying keyspace in the TIFF
 * 6.0 standard to support the GeoTIFF extension (only one mandatory key needs to be reserved for the GKD itself).
 *
 * <p>This directory can be searched just like an {@link Ifd} for tags and returns normal {@link Entry} objects.
 *
 * <p>See the <a href="https://docs.ogc.org/is/19-008r4/19-008r4.html#_underlying_tiff_requirements">GeoTIFF</a> spec for
 * more details.
 */
public record GeoKeyDirectory(short keyDirectoryVersion,
                              short keyRevision,
                              short minorRevision,
                              short numberOfKeys,
                              Entry[] entries) {

    public static final String NAME = "GEO_KEY_DIRECTORY";

    public static final short ID = (short) 34735;

    public GeoKeyDirectory {
        checkArgument(keyDirectoryVersion == 1,
                "Currently only directory version 1 is supported, received %s", keyDirectoryVersion);
        checkArgument(keyRevision == 1,
                "Currently only key revision 1 is supported, received %s", keyRevision);
        checkArgument(minorRevision == 0 || minorRevision == 1,
                "Currently only minor revisions 0 and 1 are supported, received %s", minorRevision);
        checkArgument(numberOfKeys > 0,
                "Should be at least 1 key present in the directory, found %s", numberOfKeys);
    }

    public static GeoKeyDirectory getRequired(Ifd ifd) {
        return getOptional(ifd).orElseThrow(() -> new MissingRequiredTagException(NAME, ID));
    }

    public static Optional<GeoKeyDirectory> getOptional(Ifd ifd) {
        return switch (ifd.findTag(ID)) {
            case Entry.Short s -> Optional.of(GeoKeyDirectory.create(s.values(), ifd));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                 Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _,
                 Entry.Double _ -> throw new UnsupportedTypeForTagException(NAME, ID);
        };
    }

    static GeoKeyDirectory create(short[] shorts, Ifd ifd) {
        checkArgument(shorts.length >= 4,
                "Expected at least 4 short values for the header, found %s", shorts.length);

        GkdEntryMaker maker = new GkdEntryMaker(ifd);
        return new GeoKeyDirectory(
                shorts[0],
                shorts[1],
                shorts[2],
                shorts[3],
                maker.makeAllEntries(shorts, 4)
        );
    }

    /**
     * Find the entry associated with the provided GeoKey in the GKD, if not found return {@link Entry.NotFound}.
     *
     * @param geoKey the short id of the GeoKey to find
     */
    public Entry findKey(short geoKey) {

        Ifd.Entry searchEntry = Ifd.Entry.notFound(geoKey);
        int index = Arrays.binarySearch(entries, searchEntry);

        return index < 0 ? searchEntry : entries[index];
    }
}
