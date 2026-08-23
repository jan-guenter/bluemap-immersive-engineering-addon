# BlueMap Immersive Engineering Add-on

A Java 21 BlueMap add-on for the exact `immersiveengineering-12.4.2-194` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Release target: `0.1.0-alpha.1`. The owner accepted the staged rendering.

The add-on renders a neutral static view of the exact installed IE resources:

- 24 formed multiblock IDs, covered by 47 gallery cases;
- 19 OBJ-backed special block IDs;
- 8 special-shape IDs, with 65 special-block placements in the gallery; and
- persisted IE wire connections, with 8 spans in the accepted gallery.

The add-on reads IE models and textures from the verified installed JAR. It
does not bundle Immersive Engineering assets or binaries.

## Build

```bash
gradle --no-daemon \
  -PbluemapSourcePath=../bluemap-backport \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  clean prototypeCheck build
```

Use Gradle `9.6.1` and Java `21`. `prototypeCheck` runs the Java tests,
Checkstyle, production and sources JAR audits, exact artifact verification,
generated-gallery check, and the 47-case gallery lint. See
`provenance/upstreams.json` for the immutable artifact identity and the
[execution guide](docs/EXECUTION.md) for staging and release commands.

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
