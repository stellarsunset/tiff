package io.github.stellarsunset.tiff;

import io.github.stellarsunset.tiff.Ifd.Entry;
import io.github.stellarsunset.tiff.baseline.tag.ColorMap;
import io.github.stellarsunset.tiff.baseline.tag.ImageLength;
import io.github.stellarsunset.tiff.baseline.tag.MissingRequiredTagException;
import io.github.stellarsunset.tiff.baseline.tag.UnsupportedTypeForTagException;
import io.github.stellarsunset.tiff.extension.tag.GeoKeyDirectory;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Metadata about TIFF images is encoded in "tags", the values of tags are stored as entries inside an {@link Ifd}.
 *
 * <p>Convention for brevity/clarity/etc. is to provide a static accessor class for tags, see {@link ImageLength} or
 * {@link ColorMap}, these classes can be located via the marker interface {@link Accessor}.
 *
 * @param id   the identifier for the tag, this is used to locate {@link Entry}'s containing the value of the tag.
 * @param name the canonical name for the tag, this isn't used functionally for lookups, but is useful context to have
 *             offhand when working and therefore is included.
 */
public record Tag(short id, String name) {

    /**
     * This class exists to make it easy to find classes that provide a more semantically meaningful handle for interacting
     * with the value of a {@link Tag}.
     *
     * <p>Splitting this out makes more sense for things like, {@link ColorMap} and {@link GeoKeyDirectory} which are
     * complex and semantically meaningful values, but also provides a place to collect useful tag value extraction code.
     *
     * <p>By conventions {@link Accessor} implementations should have two static methods:
     * <ol>
     *     <li>{@code Class.get(Ifd ifd)} - returning the value of the tag or throwing a {@link MissingRequiredTagException}.</li>
     *     <li>{@code Class.getIfPresent(Ifd ifd)} - return the value of the tag if present or {@link Optional#empty()}
     *     if the value isn't present.</li>
     * </ol>
     */
    public interface Accessor {

        /**
         * Convenience, optionally returns an array of ASCII character bytes.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static Optional<byte[]> optionalAsciiArray(Tag tag, Ifd ifd) {
            Ifd.Entry entry = ifd.findTag(tag.id);
            return switch (entry) {
                case Entry.Ascii d -> Optional.of(d.values());
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                     Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                        throw new UnsupportedTypeForTagException(tag, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an unsigned short, in a {@code int} as all Java
         * primitives are implicitly signed.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static OptionalInt optionalUShort(Tag tag, Ifd ifd) {
            return optionalUShortArray(tag, ifd).map(a -> OptionalInt.of(a[0])).orElseGet(OptionalInt::empty);
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an array of unsigned shorts, in a {@code int[]}
         * as all Java primitives are implicitly signed.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static Optional<int[]> optionalUShortArray(Tag tag, Ifd ifd) {
            Ifd.Entry entry = ifd.findTag(tag.id);
            return switch (entry) {
                case Entry.Short s -> Optional.of(Arrays.toUnsignedIntArray(s.values()));
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Long _, Entry.Rational _, Entry.SByte _, Entry.Undefined _,
                     Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _, Entry.Double _ ->
                        throw new UnsupportedTypeForTagException(tag, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an unsigned integer, in a {@code long} as all
         * Java primitives are implicitly signed.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static OptionalLong optionalUInt(Tag tag, Ifd ifd) {
            return optionalUIntArray(tag, ifd).map(a -> OptionalLong.of(a[0])).orElseGet(OptionalLong::empty);
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an array of unsigned integers, in a {@code long[]}
         * as all Java primitives are implicitly signed.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static Optional<long[]> optionalUIntArray(Tag tag, Ifd ifd) {
            Ifd.Entry entry = ifd.findTag(tag.id);
            return switch (entry) {
                case Entry.Short s -> Optional.of(Arrays.toUnsignedLongArray(s.values()));
                case Entry.Long l -> Optional.of(Arrays.toUnsignedLongArray(l.values()));
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Rational _, Entry.SByte _, Entry.Undefined _, Entry.SShort _,
                     Entry.SLong _, Entry.SRational _,
                     Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(tag, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as a {@link Rational} number.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static Optional<Rational> optionalRational(Tag tag, Ifd ifd) {
            Ifd.Entry entry = ifd.findTag(tag.id);
            return switch (entry) {
                case Entry.Rational r -> Optional.of(r.rational(0));
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.SByte _, Entry.Undefined _,
                     Entry.SShort _, Entry.SLong _, Entry.SRational _,
                     Entry.Float _, Entry.Double _ -> throw new UnsupportedTypeForTagException(tag, entry.getClass());
            };
        }

        /**
         * Convenience, optionally returns the value of the provided tag as an array of {@code double}s.
         *
         * @param tag the tag value to access
         * @param ifd the {@link Ifd} to locate the tag in
         */
        static Optional<double[]> optionalDoubleArray(Tag tag, Ifd ifd) {
            Ifd.Entry entry = ifd.findTag(tag.id);
            return switch (entry) {
                case Entry.Double d -> Optional.of(d.values());
                case Entry.NotFound _ -> Optional.empty();
                case Entry.Byte _, Entry.Ascii _, Entry.Short _, Entry.Long _, Entry.Rational _, Entry.SByte _,
                     Entry.Undefined _, Entry.SShort _, Entry.SLong _, Entry.SRational _, Entry.Float _ ->
                        throw new UnsupportedTypeForTagException(tag, entry.getClass());
            };
        }
    }
}
