# Releasing

Releases require a clean reviewed commit and an annotated tag whose name is
exactly `v<runtime_version>`.

1. Run `clean check verifyPublication` with Gradle 9.4.0 and 9.6.1 on Java 21.
2. Run the Gradle 9.6.1 publication build twice from clean state and compare
   the production JAR, sources JAR, POM, and module metadata byte for byte.
3. Inspect both archives and confirm `verifyRuntimeJar`,
   `verifyRuntimeSourcesJar`, and `verifyOriginSources` passed.
4. Confirm the POM and module metadata contain no dependency.
5. Merge the reviewed version commit.
6. Create and push an annotated `v<runtime_version>` tag at that commit.
7. Let the release workflow create a draft, upload and attest the exact files,
   publish the Maven package, download the assets for comparison, and then
   publish the prerelease or release.

The workflow supports a manual resume only for the same immutable annotated
tag. A successful build does not authorize any consumer update or server
deployment.
