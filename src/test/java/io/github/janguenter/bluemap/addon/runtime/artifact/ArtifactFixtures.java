/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.addon.runtime.artifact;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ArtifactFixtures {

    private static final String DESCRIPTOR = "META-INF/neoforge.mods.toml";

    private ArtifactFixtures() {
    }

    static Path jar(Path directory, String name, String modId, String payload)
            throws IOException {
        return jarWithDescriptor(
                directory,
                name,
                ("modLoader=\"javafml\"\n"
                        + "# comments and quoted # values must not confuse admission\n"
                        + "[[mods]]\n"
                        + "modId=\"" + modId + "\"\n"
                        + "displayName=\"Example # literal\"\n")
                        .getBytes(StandardCharsets.UTF_8),
                payload
        );
    }

    static Path jarWithDescriptor(
            Path directory,
            String name,
            byte[] descriptor,
            String payload
    ) throws IOException {
        Path jar = directory.resolve(name);
        try (OutputStream output = Files.newOutputStream(jar);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            if (descriptor != null) {
                zip.putNextEntry(new ZipEntry(DESCRIPTOR));
                zip.write(descriptor);
                zip.closeEntry();
            }
            zip.putNextEntry(new ZipEntry("payload.txt"));
            zip.write(payload.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return jar;
    }

    static ArtifactPin pin(Path jar, String key, String modId) throws IOException {
        return new ArtifactPin(
                key,
                modId,
                "1.0.0",
                jar.getFileName().toString(),
                Files.size(jar),
                digest(jar)
        );
    }

    static String digest(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
