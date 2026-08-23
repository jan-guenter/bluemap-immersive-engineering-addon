# Agent guide for the Immersive Engineering BlueMap add-on

This is an independent public add-on repository coordinated from the private
All the Mons workspace. Read this file and `README.md` before changing it.

## Exact baseline

- All the Mons `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9`
- Minecraft `1.21.1`
- NeoForge `21.1.248`
- Java `21`
- BlueMap `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d`
- BlueMap API commit `285c9a60eff3ac2b0cab308ce1058d1565be0971`
- Exact profile `immersiveengineering-12.4.2-194`

This is a standalone BlueMap add-on, not a NeoForge mod. Do not add client
classes, candidate binaries/assets/source, nested JARs, Minecraft classes,
Mixins, or world state.

## Development contract

- Preserve stock rendering while the runtime/profile is absent, duplicated,
  unsupported, malformed, or disabled.
- Keep the BlueMap internal API behind `adapter/bluemap522`.
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
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

Run the full exact-profile development gate with Gradle `9.6.1` and Java `21`:

```bash
gradle --no-daemon \
  -PbluemapSourcePath=../bluemap-backport \
  -PimmersiveEngineeringJar=/path/to/ImmersiveEngineering-1.21.1-12.4.2-194.jar \
  clean prototypeCheck build
```

Run `verifyReleaseCandidate -PreleaseTag=v0.1.0-alpha.1` only after sealing
the owner-accepted staging entries and final artifacts. Follow
`docs/EXECUTION.md` for the exact promotion and publication sequence.

Never stage or commit generated build output, candidate JARs, galleries, worlds,
credentials, logs, or research evidence.
