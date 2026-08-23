/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import de.bluecolored.bluemap.core.util.Key;

import java.util.Map;
import java.util.Set;

/** One static formed multiblock model compiled from the admitted IE JAR. */
public record InstalledMultiblockModel(
        String blockId,
        WavefrontModel model,
        Map<String, Key> materials,
        Set<PartPosition> parts
) {

    public InstalledMultiblockModel {
        materials = Map.copyOf(materials);
        parts = Set.copyOf(parts);
    }

    /** Position relative to the formed structure's master block. */
    public record PartPosition(int x, int y, int z) {
    }
}
