# Add-on execution

The accepted renderer covers the exact Immersive Engineering `12.4.2-194`
profile. It reads installed models and textures after exact JAR verification
and falls back to stock BlueMap rendering for unsupported or malformed data.

Clone with `--recurse-submodules`, or initialize both exact source submodules:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
```

The settings preflight rejects uninitialized, changed, dirty, or mismatched
toolkit and Adapter API checkouts. Before running Gradle gates, activate a Python
3.11 or newer virtual environment, install the hash-locked toolkit wheel, and
verify the repository contract:

```bash
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
bluemap-addon-toolkit conventions check .
```

The requirement locks the 20,585-byte `v0.3.0-alpha.1` wheel at SHA-256
`82f1ec53603646849a7c2d4b58f3fb7000413fe83043a302bee88cc88daeb8f7`.

## Development gate

Acquire the exact candidate JAR outside Git. Use Gradle `9.6.1` and Java `21`.
The candidate property is:

- `-PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar`

Then run:

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
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
the matching client. The migration target is `0.1.0-alpha.2`.

## Acceptance and release

The migration candidate records the production JAR, sources JAR, POM, and
Gradle module identities under `candidate_artifacts`. After visual acceptance,
change the provenance status to `owner-accepted-release-candidate` and record
the accepted combined integration run without changing those artifact bytes.

Run the exact candidate gate through a pull request:

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  -PreleaseTag=v0.1.0-alpha.2 \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after owner acceptance and final-head CI passes this gate. Create an
annotated `v0.1.0-alpha.2` tag at reviewed `main`. The release workflow checks the tag,
exact BlueMap checkout, accepted bytes, and draft assets before making the
prerelease public. Publication never deploys to production.
