# Changelog

## 0.1.0-alpha.3 - 2026-09-02

- Use separate released shared Adapter API plans for base and block-entity
  registrations without changing ordering, failure reasons, or rendering.

## 0.1.0-alpha.2 - 2026-09-02

- Migrated exact runtime admission and the internal adapter to the tested
  BlueMap 5.23 feature backport.
- Replaced local compatibility, registry, extension-factory, and synthetic
  dispatch helpers with the pinned four-source Adapter API module.
- Preserved all accepted multiblock, OBJ-special, special-shape, coke-oven,
  and persisted-wire rendering behavior.

## 0.1.0-alpha.1 - 2026-08-23

- Render 24 formed Immersive Engineering multiblock IDs from their installed
  OBJ resources, covering the accepted 47-case gallery.
- Render the accepted installed-model and special-shape blocks, including
  conveyors, feedthroughs and fluid pipes.
- Render persisted Immersive Engineering wire spans as static catenaries.
- Keep unsupported artifacts, states and malformed data on bounded stock
  fallback paths.
