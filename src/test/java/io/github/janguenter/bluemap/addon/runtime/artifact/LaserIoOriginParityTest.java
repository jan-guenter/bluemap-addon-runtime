/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.addon.runtime.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LaserIoOriginParityTest {

    @TempDir
    Path temporary;

    @Test
    void matchesTheFrozenLaserIoDetectorAcrossDescriptorAndRootCases() throws IOException {
        List<byte[]> descriptors = List.of(
                "[[mods]]\nmodId=\"targetmod\"\n".getBytes(StandardCharsets.UTF_8),
                "[[mods]]\nmodId='targetmod'\n".getBytes(StandardCharsets.UTF_8),
                "[['mods']]\n'modId'='targetmod'\n".getBytes(StandardCharsets.UTF_8),
                ("description='''\n[[mods]]\nmodId='fake'\n'''\n"
                        + "[[mods]]\nmodId='targetmod'\n").getBytes(StandardCharsets.UTF_8),
                "# [[mods]]\n# modId='targetmod'\n".getBytes(StandardCharsets.UTF_8),
                "[dependencies.targetmod]\nmodId='targetmod'\n".getBytes(StandardCharsets.UTF_8),
                "[[mods]]\nmodId=\"targetmod\n".getBytes(StandardCharsets.UTF_8),
                new byte[] {(byte) 0xc3, (byte) 0x28}
        );

        for (int index = 0; index < descriptors.size(); index++) {
            Path jar = ArtifactFixtures.jarWithDescriptor(
                    temporary,
                    "case-" + index + ".jar",
                    descriptors.get(index),
                    "payload-" + index
            );
            assertParity(List.of(jar), jar, "targetmod");
        }

        Path exact = ArtifactFixtures.jar(temporary, "exact.jar", "targetmod", "payload");
        Path duplicate = temporary.resolve("duplicate.jar");
        Files.copy(exact, duplicate);
        assertParity(List.of(exact, duplicate), exact, "targetmod");
        assertParity(Arrays.asList(null, temporary, exact, exact), exact, "targetmod");
        assertParity(List.of(exact), exact, "anothermod");
    }

    private static void assertParity(List<Path> roots, Path pinned, String modId)
            throws IOException {
        ArtifactPin sharedPin = ArtifactFixtures.pin(pinned, "target", modId);
        io.github.janguenter.bluemap.laserio.profile.ArtifactPin originPin =
                new io.github.janguenter.bluemap.laserio.profile.ArtifactPin(
                        sharedPin.key(),
                        sharedPin.modId(),
                        sharedPin.version(),
                        sharedPin.fileName(),
                        sharedPin.size(),
                        sharedPin.sha256()
                );
        Outcome shared = outcome(() -> ExactArtifactDetector.matchesAll(
                roots, List.of(sharedPin)
        ));
        Outcome origin = outcome(() ->
                io.github.janguenter.bluemap.laserio.profile.ExactArtifactDetector
                        .matchesAll(roots, List.of(originPin))
        );
        assertEquals(origin, shared);
    }

    private static Outcome outcome(BooleanSupplier action) {
        try {
            return new Outcome(action.getAsBoolean(), null, null);
        } catch (RuntimeException exception) {
            return new Outcome(null, exception.getClass(), exception.getMessage());
        }
    }

    @Test
    void matchesTheFrozenLaserIoDetectorAtTheBound() throws IOException {
        Path exact = ArtifactFixtures.jar(temporary, "exact.jar", "targetmod", "payload");
        List<Path> roots = new ArrayList<>();
        for (int index = 0; index < 4_096; index++) {
            roots.add(temporary.resolve("absent-" + index));
        }
        roots.add(exact);
        assertParity(roots, exact, "targetmod");
    }

    private record Outcome(Boolean result, Class<? extends RuntimeException> failure, String message) {
    }
}
