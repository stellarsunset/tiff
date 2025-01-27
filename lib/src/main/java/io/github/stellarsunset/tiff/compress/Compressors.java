package io.github.stellarsunset.tiff.compress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.joining;

public final class Compressors {

    private final ConcurrentHashMap<Integer, Compressor> registry;

    private Compressors() {
        this.registry = createRegistry();
    }

    public static Compressors getInstance() {
        return Helper.INSTANCE;
    }

    /**
     * Returns the {@link Compressor} instance registered with the given int code (unsigned short).
     *
     * @param code the int id of the compressor instance to return
     */
    public Compressor compressorFor(int code) {
        Compressor compressor = registry.get(code);
        if (compressor == null) {

            String known = registry.keySet().stream()
                    .map(i -> Integer.toString(i)).collect(joining(","));

            String message = String.format("Unable to locate compressor for code: %s. Known codes are: %s.",
                    code,
                    known
            );

            throw new IllegalArgumentException(message);
        }
        return compressor;
    }

    /**
     * Register a new compressor for a given short code, clients should use this if a compression type not supported by
     * default is encountered.
     *
     * @param code       the unsigned short id of the compressor
     * @param compressor the compressor instance to use when the code is encountered
     */
    public Compressors registerCompressor(int code, Compressor compressor) {
        registry.put(code, compressor);
        return this;
    }

    private static ConcurrentHashMap<Integer, Compressor> createRegistry() {

        Map<Integer, Compressor> baseline = Map.of(
                1, Compressor.uncompressed(),
                2, Compressor.modifiedHuffman(),
                5, Compressor.lzw(),
                32773, Compressor.packBits()
        );

        return new ConcurrentHashMap<>(baseline);
    }

    private static final class Helper {
        private static final Compressors INSTANCE = new Compressors();
    }
}
