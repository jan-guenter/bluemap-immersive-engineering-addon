#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Unit tests for the authored Immersive Engineering gallery contract."""

from __future__ import annotations

import itertools
import unittest

import cases


FORMED_IDS = frozenset(
    (
        "coke_oven",
        "blast_furnace",
        "advanced_blast_furnace",
        "alloy_smelter",
        "lightning_rod",
        "crusher",
        "fermenter",
        "diesel_generator",
        "metal_press",
        "assembler",
        "auto_workbench",
        "bottling_machine",
        "silo",
        "shelf",
        "tank",
        "mixer",
        "refinery",
        "squeezer",
        "bucket_wheel",
        "excavator",
        "sawmill",
        "arc_furnace",
        "radio_tower",
        "chunk_loader",
    )
)

MIRRORABLE_IDS = frozenset(
    (
        "crusher",
        "fermenter",
        "diesel_generator",
        "metal_press",
        "auto_workbench",
        "bottling_machine",
        "mixer",
        "refinery",
        "squeezer",
        "excavator",
        "sawmill",
        "arc_furnace",
        "radio_tower",
        "chunk_loader",
    )
)

EAST_CONTROL_IDS = frozenset(
    (
        "coke_oven",
        "crusher",
        "assembler",
        "arc_furnace",
        "tank",
        "chunk_loader",
        "bucket_wheel",
        "excavator",
        "radio_tower",
    )
)

TEMPLATE_PATHS = {
    "advanced_blast_furnace": "improved_blast_furnace",
    "arc_furnace": "arcfurnace",
    "excavator": "excavator_full",
    "tank": "sheetmetal_tank",
}

FORMATION_PATHS = {
    "excavator": "excavator",
}

FORMATION_ORIGIN_OFFSETS = {
    "excavator": (0, 2, 2),
}


def overlaps(first, second) -> bool:
    return not (
        first[3] < second[0]
        or second[3] < first[0]
        or first[4] < second[1]
        or second[4] < first[1]
        or first[5] < second[2]
        or second[5] < first[2]
    )


class GalleryContractTest(unittest.TestCase):

    def test_all_formed_ids_have_one_normal_case(self):
        normal = tuple(case for case in cases.PLACEMENTS if case.role == "normal")
        self.assertEqual(24, len(normal))
        self.assertEqual(FORMED_IDS, {case.structure.formed_id for case in normal})

    def test_exact_mirrorable_set_has_one_mirrored_case(self):
        mirrored = tuple(case for case in cases.PLACEMENTS if case.mirrored)
        self.assertEqual(14, len(mirrored))
        self.assertEqual(
            MIRRORABLE_IDS,
            {case.structure.formed_id for case in mirrored},
        )

    def test_selected_families_have_east_facing_controls(self):
        east = tuple(case for case in cases.PLACEMENTS if case.role == "east-control")
        self.assertEqual(9, len(east))
        self.assertEqual(EAST_CONTROL_IDS, {case.structure.formed_id for case in east})
        self.assertTrue(all(case.facing == "east" for case in east))

    def test_pages_are_ordered_and_cases_do_not_overlap(self):
        self.assertEqual((2, 3, 4, 5), tuple(page.number for page in cases.PAGES))
        self.assertEqual(47, len(cases.PLACEMENTS))
        for first, second in itertools.combinations(cases.PLACEMENTS, 2):
            self.assertFalse(
                overlaps(first.clear_bounds(), second.clear_bounds()),
                f"clear boxes overlap: {first.case_id} and {second.case_id}",
            )

    def test_monumental_family_anchors_have_wide_spacing(self):
        normal = [case for case in cases.PAGE_5.cases if case.role == "normal"]
        for first, second in itertools.combinations(normal, 2):
            separation = max(abs(first.x - second.x), abs(first.z - second.z))
            self.assertGreaterEqual(separation, 120)

    def test_installed_template_name_exceptions_are_explicit(self):
        normal = (case for case in cases.PLACEMENTS if case.role == "normal")
        actual = {
            case.structure.formed_id: case.structure.template_path
            for case in normal
            if case.structure.template_path != case.structure.formed_id
        }
        self.assertEqual(TEMPLATE_PATHS, actual)

    def test_formation_path_exceptions_are_explicit(self):
        normal = (case for case in cases.PLACEMENTS if case.role == "normal")
        actual = {
            case.structure.formed_id: case.structure.formation_path
            for case in normal
            if case.structure.formation_path is not None
        }
        self.assertEqual(FORMATION_PATHS, actual)

    def test_formation_origin_exceptions_are_explicit(self):
        normal = (case for case in cases.PLACEMENTS if case.role == "normal")
        actual = {
            case.structure.formed_id: case.structure.formation_origin_offset
            for case in normal
            if case.structure.formation_origin_offset != (0, 0, 0)
        }
        self.assertEqual(FORMATION_ORIGIN_OFFSETS, actual)


if __name__ == "__main__":
    unittest.main()
