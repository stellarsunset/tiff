package io.github.stellarsunset.tiff.extension.tag;

import io.github.stellarsunset.tiff.Ifd;
import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import io.github.stellarsunset.tiff.extension.geokey.GeodeticCrs;
import io.github.stellarsunset.tiff.extension.geokey.ProjectedCrs;
import io.github.stellarsunset.tiff.extension.geokey.VerticalCrs;

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
 *     <li>Used like an {@link Ifd}, e.g. {@code SomeGeoKey.getOptional(gkd)}</li>
 * </ol>
 *
 * <p>This design minimizes the number of reserved tags that need to be carved out of the underlying keyspace in the TIFF
 * 6.0 standard to support the GeoTIFF extension (only one mandatory key needs to be reserved for the GKD itself).
 *
 * <p>This directory can be searched just like an {@link Ifd} for tags and returns normal {@link Entry} objects. For TIFF
 * tag-like handles for GeoKeys see the {@code ..extension.geokey} package.
 *
 * <p>Generally speaking there are three GeoKeys to start with:
 * <ol>
 *     <li>{@link GeodeticCrs} - this or the projected will be provided, see the docs on the tag accessor for details</li>
 *     <li>{@link ProjectedCrs} - this or the geodetic will be provided, see the docs on the tag accessor for details</li>
 *     <li>{@link VerticalCrs} - this is optionally provided if the values of the pixels in the raster required further
 *     details for interpretation, e.g. if they are elevations</li>
 * </ol>
 *
 * <p>See the <a href="https://docs.ogc.org/is/19-008r4/19-008r4.html#_underlying_tiff_requirements">GeoTIFF</a> spec or
 * the <a href="http://geotiff.maptools.org/spec/geotiff6.html">GeoTIFF maptools</a> link more tag info.
 */
public record GeoKeyDirectory(short keyDirectoryVersion,
                              short keyRevision,
                              short minorRevision,
                              short numberOfKeys,
                              Entry[] entries) implements Tag.Accessor {

    public static final Tag TAG = new Tag((short) 0x87AF, "GEO_KEY_DIRECTORY");

    public GeoKeyDirectory {
        checkArgument(keyDirectoryVersion == 1,
                "Currently only directory version 1 is supported, received %s", keyDirectoryVersion);
        checkArgument(keyRevision == 1,
                "Currently only key revision 1 is supported, received %s", keyRevision);
        checkArgument(minorRevision == 0 || minorRevision == 1,
                "Currently only minor revisions 0 and 1 are supported, received %s", minorRevision);
        checkArgument(Short.toUnsignedInt(numberOfKeys) == entries.length,
                "Should be the same number of keys %s as entries %s", numberOfKeys, entries.length);
    }

    public static GeoKeyDirectory v1(Entry[] entries) {
        return new GeoKeyDirectory((short) 1, (short) 1, (short) 1, (short) entries.length, entries);
    }

    public static GeoKeyDirectory get(Ifd ifd) {
        return getIfPresent(ifd).orElseThrow(() -> new MissingRequiredTagException(TAG));
    }

    public static Optional<GeoKeyDirectory> getIfPresent(Ifd ifd) {
        Ifd.Entry entry = ifd.findTag(TAG.id());
        return switch (entry) {
            case Entry.Short s -> Optional.of(GeoKeyDirectory.create(s.values(), ifd));
            case Entry.NotFound _ -> Optional.empty();
            case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                 Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _,
                 Entry.Double _ -> throw new UnsupportedTypeForTagException(TAG, entry.getClass());
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
