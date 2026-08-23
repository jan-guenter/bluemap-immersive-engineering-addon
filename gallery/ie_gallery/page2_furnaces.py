# SPDX-License-Identifier: MIT
"""Page 2: the four registered stone multiblocks."""

from __future__ import annotations

from itertools import chain

from ie_gallery.model import GalleryPage, Structure, variants


STRUCTURES = (
    Structure("coke_oven", "coke_oven", (3, 3, 3), False),
    Structure("blast_furnace", "blast_furnace", (3, 3, 3), False),
    Structure(
        "advanced_blast_furnace",
        "improved_blast_furnace",
        (3, 4, 3),
        False,
    ),
    Structure("alloy_smelter", "alloy_smelter", (2, 2, 2), False),
)

CASES = tuple(
    chain.from_iterable(
        variants(
            2,
            structure,
            160 + index * 48,
            160,
            east_control=structure.formed_id == "coke_oven",
        )
        for index, structure in enumerate(STRUCTURES)
    )
)

PAGE = GalleryPage(2, "stone furnaces", CASES)
