# SPDX-License-Identifier: MIT
"""Page 4: storage, display, and chunk-loading structures."""

from __future__ import annotations

from itertools import chain

from ie_gallery.model import GalleryPage, Structure, variants


STRUCTURES = (
    Structure("silo", "silo", (3, 7, 3), False),
    Structure("shelf", "shelf", (4, 4, 2), False),
    Structure("tank", "sheetmetal_tank", (3, 5, 3), False),
    Structure("chunk_loader", "chunk_loader", (3, 5, 3), True),
)

EAST_CONTROLS = frozenset(("tank", "chunk_loader"))


def _cases_for(index: int, structure: Structure):
    column = index % 2
    row = index // 2
    return variants(
        4,
        structure,
        160 + column * 64,
        416 + row * 32,
        east_control=structure.formed_id in EAST_CONTROLS,
    )


CASES = tuple(
    chain.from_iterable(
        _cases_for(index, structure) for index, structure in enumerate(STRUCTURES)
    )
)

PAGE = GalleryPage(4, "storage and utility structures", CASES)
