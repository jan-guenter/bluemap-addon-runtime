# Architecture

## Packaging

Each BlueMap add-on compiles the shared Java sources into its own production
JAR. A consumer pins this repository as source, usually through a Git
submodule. This keeps class identity local to the consumer's BlueMap
classloader and avoids an installed provider whose version BlueMap cannot
constrain.

The standalone JAR, sources JAR, POM, and Gradle module metadata are review and
distribution artifacts. The production JAR is not a server component. The POM
and module metadata intentionally declare no dependency.

## Artifact admission

`ArtifactPin` holds the consumer-owned identity fields. It validates their
shape but does not fetch or discover artifacts.

`ExactArtifactDetector.matchesAll` inspects a bounded iterable of paths. For
each pin it:

1. admits regular files with a `.jar` suffix;
2. resolves real paths and rejects ambiguous duplicate declarations;
3. parses at most one MiB of `META-INF/neoforge.mods.toml` as strict UTF-8;
4. requires the requested `modId` inside a `[[mods]]` table; and
5. compares the complete file size and SHA-256 digest.

Every pin must select a different real file. I/O errors and malformed
descriptors return `false`. Invalid or duplicate pin lists throw
`IllegalArgumentException`. Null containers or elements throw
`NullPointerException`. These results match the origin implementation.

## Deliberate exclusions

This release has no diagnostics, activation state, registry checks, profile
selection, resource loading, BlueMap adapter, candidate coordinates, or
download logic. Those need separate extraction and parity work.
