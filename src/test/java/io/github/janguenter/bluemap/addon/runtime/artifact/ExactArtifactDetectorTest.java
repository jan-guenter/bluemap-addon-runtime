/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.addon.runtime.artifact;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExactArtifactDetectorTest {

    @TempDir
    Path temporary;

    @Test
    void acceptsOneExactDeclaringJar() throws IOException {
        Path jar = ArtifactFixtures.jar(temporary, "candidate.jar", "targetmod", "payload");
        ArtifactPin pin = ArtifactFixtures.pin(jar, "target", "targetmod");

        assertTrue(ExactArtifactDetector.matchesAll(List.of(jar), List.of(pin)));
    }

    @Test
    void rejectsDuplicatesWrongBytesAndWrongDeclarations() throws IOException {
        Path first = ArtifactFixtures.jar(temporary, "first.jar", "targetmod", "payload");
        Path duplicate = temporary.resolve("duplicate.jar");
        Files.copy(first, duplicate);
        ArtifactPin pin = ArtifactFixtures.pin(first, "target", "targetmod");
        Path wrongMod = ArtifactFixtures.jar(
                temporary, "wrong-mod.jar", "anothermod", "payload"
        );
        Path wrongBytes = ArtifactFixtures.jar(
                temporary, "wrong-bytes.jar", "targetmod", "changed"
        );

        assertFalse(ExactArtifactDetector.matchesAll(
                List.of(first, duplicate),
                List.of(pin)
        ));
        assertFalse(ExactArtifactDetector.matchesAll(List.of(wrongMod), List.of(pin)));
        assertFalse(ExactArtifactDetector.matchesAll(List.of(wrongBytes), List.of(pin)));
    }

    @Test
    void admitsDistinctPinsAndFiles() throws IOException {
        Path first = ArtifactFixtures.jar(temporary, "first.jar", "firstmod", "one");
        Path second = ArtifactFixtures.jar(temporary, "second.jar", "secondmod", "two");
        List<ArtifactPin> pins = List.of(
                ArtifactFixtures.pin(first, "first", "firstmod"),
                ArtifactFixtures.pin(second, "second", "secondmod")
        );

        assertTrue(ExactArtifactDetector.matchesAll(
                List.of(temporary, first, second),
                pins
        ));
    }

    @Test
    void rejectsNullArgumentsAndElements() throws IOException {
        Path jar = ArtifactFixtures.jar(temporary, "candidate.jar", "targetmod", "payload");
        ArtifactPin pin = ArtifactFixtures.pin(jar, "target", "targetmod");

        assertThrows(NullPointerException.class,
                () -> ExactArtifactDetector.matchesAll(null, List.of(pin)));
        assertThrows(NullPointerException.class,
                () -> ExactArtifactDetector.matchesAll(List.of(jar), null));
        assertThrows(NullPointerException.class,
                () -> ExactArtifactDetector.matchesAll(
                        Arrays.asList(jar, null), List.of(pin)
                ));
        assertThrows(NullPointerException.class,
                () -> ExactArtifactDetector.matchesAll(
                        List.of(jar), Arrays.asList(pin, null)
                ));
    }

    @Test
    void rejectsEmptyDuplicateAndReusedPins() throws IOException {
        Path jar = ArtifactFixtures.jar(temporary, "candidate.jar", "targetmod", "payload");
        ArtifactPin pin = ArtifactFixtures.pin(jar, "target", "targetmod");
        ArtifactPin otherKey = ArtifactFixtures.pin(jar, "other", "targetmod");

        assertThrows(IllegalArgumentException.class,
                () -> ExactArtifactDetector.matchesAll(List.of(jar), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> ExactArtifactDetector.matchesAll(List.of(jar), List.of(pin, pin)));
        assertFalse(ExactArtifactDetector.matchesAll(
                List.of(jar),
                List.of(pin, otherKey)
        ));
    }

    @Test
    void understandsQuotedTablesKeysBomCommentsAndMultilineStrings() throws IOException {
        String descriptor = "\ufeffmodLoader='javafml'\n"
                + "description=\"\"\"\n[[mods]]\nmodId='fake'\n\"\"\"\n"
                + "[[\"mods\"]] # table comment\n"
                + "'modId' = 'targetmod' # declaration comment\n";
        Path jar = ArtifactFixtures.jarWithDescriptor(
                temporary,
                "quoted.jar",
                descriptor.getBytes(StandardCharsets.UTF_8),
                "payload"
        );

        assertTrue(ExactArtifactDetector.matchesAll(
                List.of(jar),
                List.of(ArtifactFixtures.pin(jar, "target", "targetmod"))
        ));
    }

    @Test
    void failsClosedForMissingMalformedAndOversizedDescriptors() throws IOException {
        Path missing = ArtifactFixtures.jarWithDescriptor(
                temporary, "missing.jar", null, "payload"
        );
        Path malformed = ArtifactFixtures.jarWithDescriptor(
                temporary, "malformed.jar", new byte[] {(byte) 0xc3, (byte) 0x28}, "payload"
        );
        Path unterminated = ArtifactFixtures.jarWithDescriptor(
                temporary,
                "unterminated.jar",
                "[[mods]]\nmodId=\"targetmod\n".getBytes(StandardCharsets.UTF_8),
                "payload"
        );
        byte[] oversizedDescriptor = new byte[1024 * 1024 + 1];
        Path oversized = ArtifactFixtures.jarWithDescriptor(
                temporary, "oversized.jar", oversizedDescriptor, "payload"
        );

        for (Path jar : List.of(missing, malformed, unterminated, oversized)) {
            assertFalse(ExactArtifactDetector.matchesAll(
                    List.of(jar),
                    List.of(ArtifactFixtures.pin(jar, "target", "targetmod"))
            ));
        }
    }

    @Test
    void failsClosedAtTheRootBoundAndWhenInterrupted() throws IOException {
        Path jar = ArtifactFixtures.jar(temporary, "candidate.jar", "targetmod", "payload");
        ArtifactPin pin = ArtifactFixtures.pin(jar, "target", "targetmod");
        List<Path> tooMany = new ArrayList<>();
        for (int index = 0; index < 4_096; index++) {
            tooMany.add(temporary.resolve("absent-" + index));
        }
        tooMany.add(jar);

        assertFalse(ExactArtifactDetector.matchesAll(tooMany, List.of(pin)));
        Thread.currentThread().interrupt();
        try {
            assertFalse(ExactArtifactDetector.matchesAll(List.of(jar), List.of(pin)));
        } finally {
            Thread.interrupted();
        }
    }
}
