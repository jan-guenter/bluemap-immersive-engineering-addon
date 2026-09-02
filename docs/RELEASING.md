# Releasing

The unpublished `0.1.0-alpha.2` migration preserves the accepted 24 formed
multiblock IDs across 47 cases, 19 OBJ-backed special IDs, 8 special-shape IDs,
65 special placements, and 8 persisted wire spans.

After owner acceptance:

1. Initialize and verify the exact development-only toolkit before Gradle:

   ```bash
   git submodule update --init --recursive -- \
     tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
   python -m pip install --disable-pip-version-check --no-deps \
     --require-hashes --only-binary=:all: \
     --requirement requirements/toolkit.txt
   bluemap-addon-toolkit conventions check .
   ```

2. Record the accepted combined integration run and set the provenance status
   to `owner-accepted-release-candidate`.
3. Require the production JAR, sources JAR, POM, and Gradle module metadata to
   match the already sealed `candidate_artifacts` bytes.
4. Run `verifyReleaseCandidate -PreleaseTag=v0.1.0-alpha.2` with the exact IE
   JAR Gradle property.
5. Merge the reviewed commit, create an annotated `v0.1.0-alpha.2` tag at that
   commit, and let `.github/workflows/release.yml` publish.
6. Compare every downloaded release asset to the locally accepted bytes.
7. Update the private root portfolio, queue, and `workspace.json` in a separate
   orchestration commit.

The release workflow refuses an unpublished migration status. The tag must
equal `v0.1.0-alpha.2`. No release authorizes production
deployment.

The command sequence and required release-provenance fields are recorded in
[`EXECUTION.md`](EXECUTION.md).
