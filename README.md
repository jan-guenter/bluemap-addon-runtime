# BlueMap add-on runtime

This Java 21 source module holds runtime code shared by independent BlueMap
add-ons. Version `0.1.0-alpha.1` contains only `ArtifactPin` and
`ExactArtifactDetector` in the neutral
`io.github.janguenter.bluemap.addon.runtime.artifact` package.

The module has no BlueMap, Minecraft, NeoForge, or third-party production
dependency. It is not a BlueMap add-on or a NeoForge mod. Its archives contain
no add-on descriptor, entrypoint, service registration, `module-info`, nested
JAR, or mod metadata.

## Consumer model

BlueMap gives add-ons separate classloaders and does not provide a dependable
shared-library version contract. Add this repository at an exact commit as a
Git submodule, then compile `src/main/java` into the consumer's production
source set. Do not install `bluemap-addon-runtime-*.jar` beside BlueMap and do
not declare it as a server runtime dependency.

For a consumer with the submodule at `modules/bluemap-addon-runtime`, the
minimal Gradle wiring is:

```groovy
sourceSets {
    main.java.srcDir 'modules/bluemap-addon-runtime/src/main/java'
}
```

The consumer must pin and verify the committed submodule identity before
compilation. It must also audit its final add-on JAR so the two shared classes
are present once and no standalone runtime JAR is nested.

## Build

Use Java 21 and either Gradle 9.4.0 or 9.6.1. The repository deliberately has
no wrapper because the compatibility gate runs both versions.

```bash
gradle --no-daemon clean check verifyPublication
```

`check` runs the contracts, the LaserIO differential oracle, Checkstyle,
origin-source checks, and archive boundary checks. `verifyPublication`
additionally checks the generated POM and Gradle module metadata. The release
workflow rebuilds each output twice and compares the bytes. Gradle 9.6.1 is
the release build version because Gradle module metadata records its producer.

## API boundary

`ArtifactPin` validates one complete artifact identity. The detector returns
`true` only when each unique pin has one distinct, regular, NeoForge JAR that
declares the requested mod ID and matches the pinned size and SHA-256 value.
It fails closed on malformed descriptors, duplicate declarations, interrupted
inspection, unreadable files, oversized descriptors, and more than 4,096
roots. Callers must pass non-null paths and pins, matching the origin contract.

Exact mod IDs, versions, filenames, sizes, hashes, and profile decisions stay
in each consumer. This repository does not contain pack-specific pins.
