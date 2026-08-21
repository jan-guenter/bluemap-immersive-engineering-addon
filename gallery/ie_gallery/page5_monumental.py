# SPDX-License-Identifier: MIT
"""Page 5: tall or wide structures with 120-block family spacing."""

from __future__ import annotations

from itertools import chain

from ie_gallery.model import GalleryPage, Structure, variants


STRUCTURES = (
    Structure("lightning_rod", "lightning_rod", (3, 3, 3), False),
    Structure("bucket_wheel", "bucket_wheel", (7, 7, 1), False),
    Structure("excavator", "excavator", (3, 3, 6), True),
    Structure("radio_tower", "radio_tower", (5, 19, 6), True),
)

EAST_CONTROLS = frozenset(("bucket_wheel", "excavator", "radio_tower"))


def _cases_for(index: int, structure: Structure):
    column = index % 2
    row = index // 2
    return variants(
        5,
        structure,
        160 + column * 120,
        544 + row * 120,
        east_control=structure.formed_id in EAST_CONTROLS,
        variant_step=32,
    )


CASES = tuple(
    chain.from_iterable(
        _cases_for(index, structure) for index, structure in enumerate(STRUCTURES)
    )
)

PAGE = GalleryPage(5, "monumental structures", CASES)
