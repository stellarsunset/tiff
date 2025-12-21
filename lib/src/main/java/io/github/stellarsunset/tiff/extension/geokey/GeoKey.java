package io.github.stellarsunset.tiff.extension.geokey;

import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.Rational;
import io.github.stellarsunset.tiff.Tag;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Identical to the {@link Tag} class but for accessing entries inside a {@link GeoKeyDirectory} (GKD).
 */
public record GeoKey(short id, String name) {

    /**
     * Marker interface like {@link Tag.Accessor} for helper classes accessing GeoKey values in a GKD.
     */
    public interface Accessor {

        /**
         * Convenience, optionally returns an array of ASCII character bytes.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static Optional<byte[]> optionalAsciiArray(GeoKey key, GeoKeyDirectory gkd) {
            Entry entry = gkd.findKey(key.id);
            return switch (entry) {
                case Entry.Ascii d -> Optional.of(d.values());
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                     Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                        throw new UnsupportedTypeForGeoKeyException(key, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an unsigned short, in a {@code int} as all Java
         * primitives are implicitly signed.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static OptionalInt optionalUShort(GeoKey key, GeoKeyDirectory gkd) {
            return optionalUShortArray(key, gkd).map(a -> OptionalInt.of(a[0])).orElseGet(OptionalInt::empty);
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an array of unsigned shorts, in a {@code int[]}
         * as all Java primitives are implicitly signed.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static Optional<int[]> optionalUShortArray(GeoKey key, GeoKeyDirectory gkd) {
            Entry entry = gkd.findKey(key.id);
            return switch (entry) {
                case Entry.Short s -> Optional.of(Arrays.toUnsignedIntArray(s.values()));
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                     Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                        throw new UnsupportedTypeForGeoKeyException(key, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an unsigned integer, in a {@code long} as all
         * Java primitives are implicitly signed.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static OptionalLong optionalUInt(GeoKey key, GeoKeyDirectory gkd) {
            return optionalUIntArray(key, gkd).map(a -> OptionalLong.of(a[0])).orElseGet(OptionalLong::empty);
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an array of unsigned integers, in a {@code long[]}
         * as all Java primitives are implicitly signed.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static Optional<long[]> optionalUIntArray(GeoKey key, GeoKeyDirectory gkd) {
            Entry entry = gkd.findKey(key.id);
            return switch (entry) {
                case Entry.Short s -> Optional.of(Arrays.toUnsignedLongArray(s.values()));
                case Entry.Long l -> Optional.of(Arrays.toUnsignedLongArray(l.values()));
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _,
                     Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                        throw new UnsupportedTypeForGeoKeyException(key, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as a {@link Rational} number.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static Optional<Rational> optionalRational(GeoKey key, GeoKeyDirectory gkd) {
            Entry entry = gkd.findKey(key.id);
            return switch (entry) {
                case Entry.Rational r -> Optional.of(r.rational(0));
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.SByte _, Entry.Undefined _,
                     Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                        throw new UnsupportedTypeForGeoKeyException(key, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an array of {@code double}s.
         *
         * @param key the key value to access
         * @param gkd the {@link GeoKeyDirectory} to locate the key in
         */
        static Optional<double[]> optionalDoubleArray(GeoKey key, GeoKeyDirectory gkd) {
            Entry entry = gkd.findKey(key.id);
            return switch (entry) {
                case Entry.Double d -> Optional.of(d.values());
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                     Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _ ->
                        throw new UnsupportedTypeForGeoKeyException(key, entry.getClass());
            };
        }
    }
}
