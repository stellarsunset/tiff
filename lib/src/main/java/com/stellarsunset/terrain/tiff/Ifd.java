package com.stellarsunset.terrain.tiff;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Record representing an Image File Directory (IFD) within a TIFF file.
 *
 * <p>An Image File Directory (IFD) consists of a 2-byte count of the number of directory entries (i.e., the number of
 * fields), followed by a sequence of 12-byte field entries, followed by a 4-byte offset of the next IFD (or 0 if none).
 *
 * <p>In this implementation we eagerly materialize the value(s) associated with these entries into the {@link #entries()}
 * objects for direct access.
 *
 * <p>When reading entry values associated with a particular tag clients are expected to switch over the entry by type
 * and handle the appropriate cases for the tag they're querying, e.g. {@code Entry.Short} for compression.
 *
 * <p>There must be at least 1 IFD in a TIFF file and each IFD must have at least one entry.
 *
 * <p>The array of IFD entries in this record is guaranteed to be sorted by the unsigned tag value of the entry.
 */
public record Ifd(short entryCount, Entry[] entries, int nextIfdOffset) {

    public long unsignedNextIfdOffset() {
        return Integer.toUnsignedLong(nextIfdOffset);
    }

    /**
     * Find the entry associated with the provided tag in the IFD, if not found return {@link Entry.NotFound}.
     *
     * @param tag the short tag id to find
     */
    public Ifd.Entry findTag(short tag) {

        Ifd.Entry searchEntry = Ifd.Entry.notFound(tag);
        int index = Arrays.binarySearch(entries, searchEntry);

        return index < 0 ? searchEntry : entries[index];
    }

    // TODO - maybe add a TiffValue type, have the entries decorate it, reuse for the pixel value returns...?
    public sealed interface Entry extends Comparable<Entry> {

        static NotFound notFound(short tag) {
            return new NotFound(tag);
        }

        short tag();

        @Override
        default int compareTo(Entry entry) {
            return java.lang.Short.compareUnsigned(tag(), entry.tag());
        }

        /**
         * Indicates there was no entry associated with the given tag code in the IFD.
         *
         * <p>This will never be present in an IFD record, but may be the return value of queries for the entry associated
         * with a given tag ID in a TIFF file.
         */
        record NotFound(short tag) implements Entry {
        }

        /**
         * 8-bit unsigned integer.
         *
         * <p>To deal with as unsigned use {@link java.lang.Byte#toUnsignedInt(byte)}.
         */
        record Byte(short tag, byte[] values) implements Entry {
            public Byte {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL (binary zero)
         */
        record Ascii(short tag, byte[] values) implements Entry {
            public Ascii {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * 16-bit (2-byte) unsigned integer.
         *
         * <p>To deal with as unsigned use {@link java.lang.Short#toUnsignedInt(short)}.
         */
        record Short(short tag, short[] values) implements Entry {
            public Short {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * 32-bit (4-byte) unsigned integer
         *
         * <p>To deal with as unsigned use {@link java.lang.Integer#toUnsignedLong(int)}.
         */
        record Long(short tag, int[] values) implements Entry {
            public Long {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * Two {@link Long}s (i.e. unsigned): the first represents the numerator of a fraction; the second, the denominator.
         *
         * <p>To deal with as unsigned use {@link java.lang.Integer#toUnsignedLong(int)}.
         */
        record Rational(short tag, int[] numerators, int[] denominators) implements Entry {
            public Rational {
                checkArgument(numerators.length == denominators.length,
                        "Should have same number of numerators as denominators");
                checkArgument(numerators.length > 0, "Should be at least one value.");
            }

            public com.stellarsunset.terrain.tiff.Rational rational(int i) {
                return new com.stellarsunset.terrain.tiff.Rational(numerators[i], denominators[i]);
            }
        }

        // Since TIFF v6.0

        /**
         * An 8-bit signed (twos-complement) integer
         */
        record SByte(short tag, byte[] values) implements Entry {
            public SByte {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * An 8-bit byte that may contain anything, depending on the definition of the field
         */
        record Undefined(short tag, byte[] values) implements Entry {
            public Undefined {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * A 16-bit (2-byte) signed (twos-complement) integer
         */
        record SShort(short tag, short[] values) implements Entry {
            public SShort {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * A 32-bit (4-byte) signed (twos-complement) integer
         */
        record SLong(short tag, int[] values) implements Entry {
            public SLong {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * Two SLONG’s: the first represents the numerator of a fraction, the second the denominator.
         */
        record SRational(short tag, int[] numerators, int[] denominators) implements Entry {
            public SRational {
                checkArgument(numerators.length == denominators.length,
                        "Should have same number of numerators as denominators");
                checkArgument(numerators.length > 0, "Should be at least one value.");
            }

            public com.stellarsunset.terrain.tiff.Rational rational(int i) {
                return new com.stellarsunset.terrain.tiff.Rational(numerators[i], denominators[i]);
            }
        }

        /**
         * Single precision (4-byte) IEEE format
         */
        record Float(short tag, float[] values) implements Entry {
            public Float {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }

        /**
         * Store the length in bytes of tag values of that type to support reading.
         */
        record Double(short tag, double[] values) implements Entry {
            public Double {
                checkArgument(values.length > 0, "Should be at least one value.");
            }
        }
    }
}
