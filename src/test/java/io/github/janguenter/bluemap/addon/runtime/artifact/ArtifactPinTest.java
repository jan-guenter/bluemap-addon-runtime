/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.addon.runtime.artifact;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ArtifactPinTest {

    private static final String HASH = "0".repeat(64);

    @Test
    void acceptsTheOriginContract() {
        assertDoesNotThrow(() -> new ArtifactPin(
                "targetMod", "target_mod", "1.0.0-alpha.1",
                "target-mod+1.0.0.jar", 1, HASH
        ));
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class,
                () -> new ArtifactPin(null, "target", "1", "target.jar", 1, HASH));
        assertThrows(NullPointerException.class,
                () -> new ArtifactPin("target", null, "1", "target.jar", 1, HASH));
        assertThrows(NullPointerException.class,
                () -> new ArtifactPin("target", "target", null, "target.jar", 1, HASH));
        assertThrows(NullPointerException.class,
                () -> new ArtifactPin("target", "target", "1", null, 1, HASH));
        assertThrows(NullPointerException.class,
                () -> new ArtifactPin("target", "target", "1", "target.jar", 1, null));
    }

    @Test
    void rejectsInvalidFieldShapes() {
        assertInvalid("Target", "target", "1", "target.jar", 1, HASH);
        assertInvalid("target", "Target", "1", "target.jar", 1, HASH);
        assertInvalid("target", "target", " ", "target.jar", 1, HASH);
        assertInvalid("target", "target", "1", "../target.jar", 1, HASH);
        assertInvalid("target", "target", "1", "target.zip", 1, HASH);
        assertInvalid("target", "target", "1", "target.jar", 0, HASH);
        assertInvalid("target", "target", "1", "target.jar", 1, "A".repeat(64));
        assertInvalid("target", "target", "1", "target.jar", 1, "0".repeat(63));
    }

    private static void assertInvalid(
            String key,
            String modId,
            String version,
            String fileName,
            long size,
            String hash
    ) {
        assertThrows(IllegalArgumentException.class,
                () -> new ArtifactPin(key, modId, version, fileName, size, hash));
    }
}
