# Pilot contract

The first consumers are LaserIO, More Red, and Little Big Redstone. Each pilot
adds this repository at `modules/bluemap-addon-runtime`, pins the gitlink to the
reviewed `v0.1.0-alpha.1` commit, and compiles the module's `src/main/java`
directory into its own add-on JAR. The consumer must reject an uninitialized,
dirty, staged, or wrong-commit submodule before Gradle loads its sources.

Do not copy `bluemap-addon-runtime-0.1.0-alpha.1.jar` to a BlueMap server. No
standalone runtime JAR belongs in the server add-on directory, a consumer JAR,
or any nested-JAR metadata.

## Frozen ATMons 1.2.0 baselines

These immutable `v0.1.0-alpha.1` add-on releases are the pilot comparison
points. The input hashes identify the exact mod files used by their accepted
profiles. The add-on and gallery hashes identify the published production JAR
and deterministic gallery package.

| Consumer | Exact mod input | Input JAR SHA-256 | Add-on release commit | Add-on JAR SHA-256 | Gallery SHA-256 |
| --- | --- | --- | --- | --- | --- |
| LaserIO | LaserIO `1.9.11`, 1,305,285 bytes | `03e8537d75bc2f4ced2fc214d3409753e684d1056ee63b26db7a2b9e199ef4df` | `2148a344b1ae78e77b95aa2baa51efe46c1357e8` | `006900dd9000c6614c60b38df46b6cd1940dab53f059a381c282e9d79e89dbf1` | `acea3d7134891204f9b2246ed584fa6bfe730a7b8e3cfe2ab19b6187ddaf474b` |
| More Red | More Red `1.21.1-6.0.0.3`, 535,669 bytes | `8075126184f540c6b35b92127088f6cc4c9544627acac9f2287c62a0dfbde74e` | `845033ad8d49eab73986622dd964b6f5072a559e` | `8c146f92d2939a38093423e70dd7db248a28426fade75e5eb0cefb477dcc0f9d` | `faebaf1139ebf88b6dc5656fa57276957f6120ceea5e75007f2eb9b0df15a8b8` |
| Little Big Redstone | Little Big Redstone `1.9.8-1.21.1`, 1,415,860 bytes | `ba4eac4050528c274db4b8b43c38152ef58407298f499d28b13c97a7ca8a0896` | `cee9ed82e22e8041d53c3d9d54a61bee82da72a1` | `d3bf3ee012b5a00e3f1d546429c1f7f72a183e27e38088deaae9654a9d4750a5` | `cf2f3b370ef25ee2f3cbbef7d3c92b5a6d56e27f80985bee917346fd8a8ffe26` |

## Acceptance gates

Each pilot needs its own reviewed pull request. It must pass the complete
consumer build and release-candidate verifier against the exact mod input and
the accepted BlueMap checkout.

The archive diff must account for every changed entry. It may replace the two
consumer-local detector classes with the two source-bundled runtime classes and
the narrow consumer references needed for that package move. It must reject a
duplicate shared class, a nested runtime JAR, an unrelated archive change, or a
change to publication metadata outside the reviewed migration.

The generated gallery package must retain the baseline hash above, and the
consumer's gallery lint and checksum gates must pass. After all three isolated
gates pass, run the existing combined ATMons 1.2.0 integration suite with every
add-on installed. Each pilot must activate against its exact profile, its
individual tests must pass in the combined instance, and the integration logs
must contain no duplicate-class, linkage, or add-on loading failure.
