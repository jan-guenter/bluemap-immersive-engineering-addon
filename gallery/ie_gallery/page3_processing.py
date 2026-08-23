# SPDX-License-Identifier: MIT
"""Page 3: compact and medium processing machines."""

from __future__ import annotations

from itertools import chain

from ie_gallery.model import GalleryPage, Structure, variants


STRUCTURES = (
    Structure("crusher", "crusher", (5, 3, 3), True),
    Structure("fermenter", "fermenter", (3, 3, 3), True),
    Structure("diesel_generator", "diesel_generator", (3, 3, 5), True),
    Structure("metal_press", "metal_press", (3, 3, 1), True),
    Structure("assembler", "assembler", (3, 3, 3), False),
    Structure("auto_workbench", "auto_workbench", (3, 2, 3), True),
    Structure("bottling_machine", "bottling_machine", (3, 3, 2), True),
    Structure("mixer", "mixer", (3, 3, 3), True),
    Structure("refinery", "refinery", (5, 3, 3), True),
    Structure("squeezer", "squeezer", (3, 3, 3), True),
    Structure("sawmill", "sawmill", (5, 3, 3), True),
    Structure("arc_furnace", "arcfurnace", (5, 5, 5), True),
)

EAST_CONTROLS = frozenset(("crusher", "assembler", "arc_furnace"))


def _cases_for(index: int, structure: Structure):
    column = index % 3
    row = index // 3
    return variants(
        3,
        structure,
        160 + column * 72,
        256 + row * 32,
        east_control=structure.formed_id in EAST_CONTROLS,
    )


CASES = tuple(
    chain.from_iterable(
        _cases_for(index, structure) for index, structure in enumerate(STRUCTURES)
    )
)

PAGE = GalleryPage(3, "processing machines", CASES)
