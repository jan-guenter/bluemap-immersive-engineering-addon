# SPDX-License-Identifier: MIT
"""Small immutable types shared by the generated gallery pages."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable


BASE_Y = 100
VALID_FACINGS = frozenset(("north", "east"))


@dataclass(frozen=True)
class Structure:
    """Authored facts needed to request one installed IE multiblock."""

    formed_id: str
    template_path: str
    size: tuple[int, int, int]
    mirrorable: bool

    @property
    def block_id(self) -> str:
        return f"immersiveengineering:{self.formed_id}"

    @property
    def template_id(self) -> str:
        return f"immersiveengineering:multiblocks/{self.template_path}"


@dataclass(frozen=True)
class GalleryCase:
    """One normal, mirrored, or east-facing formed-structure request."""

    page: int
    structure: Structure
    x: int
    y: int
    z: int
    facing: str = "north"
    mirrored: bool = False
    role: str = "normal"

    def __post_init__(self) -> None:
        if self.page not in (2, 3, 4, 5):
            raise ValueError("gallery page must be in the supported 2-5 range")
        if self.facing not in VALID_FACINGS:
            raise ValueError(f"unsupported gallery facing: {self.facing}")
        if self.mirrored and not self.structure.mirrorable:
            raise ValueError(f"{self.structure.formed_id} is not mirrorable")
        if self.role not in ("normal", "mirrored", "east-control"):
            raise ValueError(f"unsupported gallery role: {self.role}")

    @property
    def case_id(self) -> str:
        suffix = {
            "normal": "north",
            "mirrored": "north-mirrored",
            "east-control": "east-control",
        }[self.role]
        return f"p{self.page}-{self.structure.formed_id.replace('_', '-')}-{suffix}"

    @property
    def label(self) -> str:
        name = self.structure.formed_id.replace("_", " ")
        if self.role == "mirrored":
            return f"{name}, north-facing mirrored"
        if self.role == "east-control":
            return f"{name}, east-facing orientation control"
        return f"{name}, north-facing normal"

    @property
    def rotation(self) -> str:
        return "clockwise_90" if self.facing == "east" else "none"

    @property
    def mirror(self) -> str:
        return "front_back" if self.mirrored else "none"

    @property
    def expected(self) -> str:
        return "formed-visible"

    def oriented_size(self) -> tuple[int, int, int]:
        size_x, size_y, size_z = self.structure.size
        if self.facing == "east":
            return size_z, size_y, size_x
        return size_x, size_y, size_z

    def clear_bounds(self, padding: int = 3) -> tuple[int, int, int, int, int, int]:
        size_x, size_y, size_z = self.oriented_size()
        horizontal_radius = max(size_x, size_z) + padding
        return (
            self.x - horizontal_radius,
            self.y - 1,
            self.z - horizontal_radius,
            self.x + horizontal_radius,
            self.y + size_y + padding - 1,
            self.z + horizontal_radius,
        )


@dataclass(frozen=True)
class GalleryPage:
    """A separately buildable and clearable review page."""

    number: int
    title: str
    cases: tuple[GalleryCase, ...]


def variants(
    page: int,
    structure: Structure,
    x: int,
    z: int,
    *,
    east_control: bool = False,
    variant_step: int = 20,
) -> tuple[GalleryCase, ...]:
    """Create the required normal case plus supported review variants."""

    result = [GalleryCase(page, structure, x, BASE_Y, z)]
    cursor = x + variant_step
    if structure.mirrorable:
        result.append(
            GalleryCase(
                page,
                structure,
                cursor,
                BASE_Y,
                z,
                mirrored=True,
                role="mirrored",
            )
        )
        cursor += variant_step
    if east_control:
        result.append(
            GalleryCase(
                page,
                structure,
                cursor,
                BASE_Y,
                z,
                facing="east",
                role="east-control",
            )
        )
    return tuple(result)


def bounds_union(
    bounds: Iterable[tuple[int, int, int, int, int, int]],
) -> tuple[int, int, int, int, int, int]:
    """Return one reporting envelope for a non-empty group of clear boxes."""

    rows = tuple(bounds)
    if not rows:
        raise ValueError("cannot combine an empty set of bounds")
    return (
        min(row[0] for row in rows),
        min(row[1] for row in rows),
        min(row[2] for row in rows),
        max(row[3] for row in rows),
        max(row[4] for row in rows),
        max(row[5] for row in rows),
    )
