# Agent guide for the Immersive Engineering BlueMap add-on

This is an independent public add-on repository coordinated from the private
All the Mons workspace. Read this file and `README.md` before changing it.

## Exact baseline

- All the Mons `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9`
- Minecraft `1.21.1`
- NeoForge `21.1.248`
- Java `21`
- BlueMap `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`
- BlueMap API commit `285c9a60eff3ac2b0cab308ce1058d1565be0971`
- Adapter API `0.1.0-alpha.2`, commit
  `e81f08bc4bfbf02d810ec8949a019130e2e61634`, source tree
  `2f974c9bb2ba13888d69682f86f30f58922d30eb`
- Exact profile `immersiveengineering-12.4.2-194`

This is a standalone BlueMap add-on, not a NeoForge mod. Do not add client
classes, candidate binaries/assets/source, nested JARs, Minecraft classes,
Mixins, or world state.

## Development contract

- Preserve stock rendering while the runtime/profile is absent, duplicated,
  unsupported, malformed, or disabled.
- Keep the BlueMap internal API behind `adapter/bluemap523`.
- Compile exactly the four pinned Adapter API helpers as source. Never install,
  bundle, or nest its standalone module JAR.
- Keep exact candidate identities and resource contracts in the profile.
- Keep state/NBT decoding, normalized data, and mesh emission separate.
- Unknown family data gets one bounded diagnostic and stock fallback.
- Use installed resources only after exact-artifact admission.
- Gallery cases and renderer facts are family-owned; do not move them back to
  the shared template.

The accepted `0.1.0-alpha.1` scope is 24 formed multiblock IDs across 47 cases,
19 installed OBJ-backed specials, 8 special-shape IDs, 65 special placements,
and 8 persisted wire spans. Render formed machines and OBJ specials in a
deterministic neutral state. Do not infer inventories, fill levels, activity,
particles, or animation phase.

## Commands

Run the quick Java, Checkstyle, and archive gate:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  clean check build
```

Run the full exact-profile development gate with Gradle `9.6.1` and Java `21`:

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  -PreleaseTag=v0.1.0-alpha.2 clean prototypeCheck build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

The owner-accepted migration candidate is sealed under `candidate_artifacts`.
Follow
`docs/EXECUTION.md` for the exact promotion and publication sequence.

Never stage or commit generated build output, candidate JARs, galleries, worlds,
credentials, logs, or research evidence.
