# BlueMap Immersive Engineering Add-on

A Java 21 BlueMap add-on for the exact `immersiveengineering-12.4.2-194` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Version `0.1.0-alpha.2` is the owner-accepted native BlueMap 5.23 release
candidate. It preserves the owner-accepted `0.1.0-alpha.1` rendering contract.

The add-on renders a neutral static view of the exact installed IE resources:

- 24 formed multiblock IDs, covered by 47 gallery cases;
- 19 OBJ-backed special block IDs;
- 8 special-shape IDs, with 65 special-block placements in the gallery; and
- persisted IE wire connections, with 8 spans in the accepted gallery.

The add-on reads IE models and textures from the verified installed JAR. It
does not bundle Immersive Engineering assets or binaries.

## Build

Clone with `--recurse-submodules`, or initialize both exact source submodules:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
```

The settings preflight rejects changed, dirty, or mismatched toolkit and
Adapter API checkouts. Install the corresponding toolkit wheel and check the
repository contract before Gradle:

```bash
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
bluemap-addon-toolkit conventions check .
```

The requirement locks the 20,585-byte `v0.3.0-alpha.1` wheel at SHA-256
`82f1ec53603646849a7c2d4b58f3fb7000413fe83043a302bee88cc88daeb8f7`.

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  clean prototypeCheck build
```

Use Gradle `9.6.1` and Java `21`. `prototypeCheck` runs the Java tests,
Checkstyle, production and sources JAR audits, exact artifact verification,
generated-gallery check, and the 47-case gallery lint. See
`provenance/upstreams.json` for the immutable artifact identity and the
[execution guide](docs/EXECUTION.md) for staging and release commands.

The four pinned Adapter API sources are compiled into the add-on. Its
standalone module JAR is neither installed nor nested.

## Install

Place the production JAR in BlueMap's add-on directory and restart the BlueMap
JVM. Removal plus one restart restores stock behavior. The add-on creates no
custom world state.

Set `-Dbluemap.immersiveengineering.disabled=true` to leave the exact profile inactive.

## Rendering boundary

Formed machines and installed OBJ specials use deterministic neutral models.
The add-on does not reproduce live contents, fill levels, activity overlays,
particles, or animation phase. If the profile is unsupported, exact JAR
verification fails, or state data is malformed, BlueMap keeps stock rendering
where available.

No Immersive Engineering binary, source, class, asset, captured mesh, or
gallery is bundled in the add-on.
