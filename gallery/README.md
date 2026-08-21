# Formed multiblock gallery

This gallery covers all 24 formed block IDs registered by the exact Immersive
Engineering `12.4.2-194` runtime. Every ID has a north-facing normal case. The
14 mirrorable IDs also have a mirrored case, and nine representative families
have an east-facing orientation control. That makes 47 cases on pages 2-5.

The definitions live in small modules under `ie_gallery/`; `cases.py` only
combines the pages. Page 5 gives the bucket wheel, excavator, and radio tower
120 blocks of family spacing so their large models remain easy to compare.

Run the deterministic checks and package the data pack with:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/test_cases.py
python gallery/lint.py
bash gallery/package.sh /tmp/immersiveengineering-gallery.zip
```

## Runtime helper contract

The repository does not copy IE structure templates. Each generated build page
uses `/place template` to read the templates installed by the exact IE JAR,
then writes one request to
`immersiveengineering_gallery:formation request` and calls:

```text
immersiveengineering_gallery_helper:form
immersiveengineering_gallery_helper:verify
```

The disposable test server must provide those two helper functions or an
equivalent command bridge. `form` must replace the placed raw template with the
requested formed structure. `verify` must report a failure unless that formed
structure is present. Both functions read this schema-1 compound:

```snbt
{
  schema: 1,
  case_id: "p3-crusher-north-mirrored",
  formed_block: "immersiveengineering:crusher",
  template: "immersiveengineering:multiblocks/crusher",
  origin: [I; 180, 100, 256],
  facing: "north",
  mirrored: 1b
}
```

The helper belongs only in disposable staging. Do not bundle it, an IE JAR,
templates, assets, source, models, textures, or captured meshes in this add-on.

Useful page commands are:

```text
/function immersiveengineering_gallery:build_page_2
/function immersiveengineering_gallery:build_page_3
/function immersiveengineering_gallery:build_page_4
/function immersiveengineering_gallery:build_page_5
/function immersiveengineering_gallery:verify
/function immersiveengineering_gallery:release
```
