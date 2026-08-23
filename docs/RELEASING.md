# Releasing

The owner accepted the staged `0.1.0-alpha.1` renderer. Its scope is 24 formed
multiblock IDs across 47 cases, 19 OBJ-backed special IDs, 8 special-shape IDs,
65 special placements, and 8 persisted wire spans. Models use deterministic
neutral states and stock-safe fallback.

Release it as follows:

1. Freeze the accepted staging JAR's non-manifest entry hashes in
   `provenance/accepted-staging-entries.sha256` with the one-time writer in
   `tools/verify_staged_equivalence.py --write`.
2. Change `addon_version` from the snapshot to `0.1.0-alpha.1` through a PR.
3. Build the production JAR, sources JAR, POM, and Gradle module metadata with the
   exact promotion Java/Gradle/BlueMap inputs.
4. Put their exact sizes and SHA-256 values in `gradle.properties` and complete
   `provenance/release.json`.
5. Run `verifyReleaseCandidate -PreleaseTag=v0.1.0-alpha.1` with the exact IE
   JAR Gradle property.
6. Merge the reviewed commit, create an annotated `v0.1.0-alpha.1` tag at that
   commit, and let `.github/workflows/release.yml` publish.
7. Compare every downloaded release asset to the locally accepted bytes.
8. Update the private root portfolio, queue, and `workspace.json` in a separate
   orchestration commit.

The tag must equal `v0.1.0-alpha.1`. No release authorizes production
deployment.

The command sequence and required release-provenance fields are recorded in
[`EXECUTION.md`](EXECUTION.md).
