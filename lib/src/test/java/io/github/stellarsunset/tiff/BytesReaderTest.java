package io.github.stellarsunset.tiff;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BytesReaderTest {

    @Test
    void test() {

        BytesReader reader = new BytesReader(
                ByteArrayChannel.fromByteArray(new byte[]{1, 2, 3, 4})
        );

        ByteBuffer buffer = reader.readBytes(0, 4);
        assertAll(
                () -> assertEquals(0, buffer.position(), "Position"),
                () -> assertEquals(4, buffer.capacity(), "Capacity")
        );
    }
}
