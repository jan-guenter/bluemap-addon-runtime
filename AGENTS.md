# Agent guide for BlueMap add-on runtime

Read this file, `README.md`, `docs/ARCHITECTURE.md`, and
`provenance/origins.json` before changing production code.

## Scope

Version `0.1.0-alpha.1` contains only `ArtifactPin` and
`ExactArtifactDetector`. Keep the production package neutral. Do not add
BlueMap, Minecraft, NeoForge, add-on, mod, service, entrypoint, or installed
shared-library behavior.

Consumers compile this repository's production source into their own add-on
JAR. They do not install or load this repository's JAR at runtime. Preserve
that classloader boundary.

## Origin contract

The two production files are exact copies of LaserIO commit
`f44e3ff20e0a4ad3b9b5dfb41e3db1d2a14a3152` except for their package
declarations. The two frozen LaserIO test-oracle files must remain byte exact.
`verifyOriginSources` enforces both rules from the hashes in
`provenance/origins.json`.

Do not change detector behavior as part of extraction. A later behavior change
needs its own version, focused tests, a consumer impact review, and documented
failure semantics.

## Required gates

Run the complete gate with both supported Gradle versions:

```bash
gradle-9.4.0 --no-daemon clean check verifyPublication
gradle-9.6.1 --no-daemon clean check verifyPublication
```

Before a release, reproduce the four publication files twice with Gradle
9.6.1 and compare every byte. Inspect the production JAR and sources JAR. Run
`actionlint .github/workflows/*.yml` after workflow changes.

Never commit build output, credentials, generated release files, consumer
JARs, or pack evidence. A version increase and release require a reviewed PR.
The release tag must be an annotated `v<runtime_version>` tag at the reviewed
commit.
