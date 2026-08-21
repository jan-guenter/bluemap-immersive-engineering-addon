#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Aggregate the family-owned Immersive Engineering gallery pages."""

from __future__ import annotations

from ie_gallery.model import bounds_union
from ie_gallery.page2_furnaces import PAGE as PAGE_2
from ie_gallery.page3_processing import PAGE as PAGE_3
from ie_gallery.page4_storage import PAGE as PAGE_4
from ie_gallery.page5_monumental import PAGE as PAGE_5


NAMESPACE = "immersiveengineering_gallery"
HELPER_NAMESPACE = "immersiveengineering_gallery_helper"
STORAGE = f"{NAMESPACE}:formation"

PAGES = (PAGE_2, PAGE_3, PAGE_4, PAGE_5)
PLACEMENTS = tuple(case for page in PAGES for case in page.cases)
ENVELOPE = bounds_union(case.clear_bounds() for case in PLACEMENTS)
