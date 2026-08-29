# Add-on execution

The accepted renderer covers the exact Immersive Engineering `12.4.2-194`
profile. It reads installed models and textures after exact JAR verification
and falls back to stock BlueMap rendering for unsupported or malformed data.

Before running Gradle gates, activate a Python 3.11 or newer virtual
environment and install the exact development-only toolkit into it:

```bash
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
```

## Development gate

Acquire the exact candidate JAR outside Git. Use Gradle `9.6.1` and Java `21`.
The candidate property is:

- `-PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar`

Then run:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  clean prototypeCheck build
bash gallery/package.sh /tmp/immersiveengineering-gallery.zip
```

`prototypeCheck` runs the unit tests, Checkstyle, production and sources JAR
audits, exact artifact verification, generated-gallery check, and gallery
lint. The gallery has 47 formed cases, 65 special placements, and 8 persisted
wire spans.

Deploy the JAR and gallery only to disposable staging. Open each intended
BlueMap view before sending its URL to the owner, then compare the render with
the matching client. The accepted target is `0.1.0-alpha.1`.

## Acceptance and release

Freeze that accepted JAR's functional entries once; the writer refuses to
overwrite an existing acceptance record:

```bash
bluemap-addon-toolkit jar-entries write \
  --jar /absolute/path/accepted-staging.jar \
  --entries provenance/accepted-staging-entries.sha256
```

Record the manifest in `provenance/release.json` as
`accepted_staging_entries` with exact `path`, `entry_count`, and `sha256`.
Record `visual_acceptance: true` under `owner_accepted_staging`, and record the
production JAR, sources JAR, POM and Gradle module file names, sizes and hashes
under `final_release_artifacts`.

Promote `addon_version` to `0.1.0-alpha.1` through a pull request and run:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  -PreleaseTag=v0.1.0-alpha.1 \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after final-version CI passes this gate. Create an annotated
`v0.1.0-alpha.1` tag at reviewed `main`. The release workflow checks the tag,
exact BlueMap checkout, accepted bytes, and draft assets before making the
prerelease public. Publication never deploys to production.
