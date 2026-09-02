/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523.SpecialShapePlan.Face;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialShapePlanTest {

    @Test
    void coversExactlyTheEightSpecialPageBlockIds() {
        assertEquals(8, SpecialShapeIntegration.BLOCK_IDS.size());
        assertEquals(6, SpecialShapeIntegration.CONVEYOR_BLOCK_IDS.size());
        assertTrue(SpecialShapeIntegration.BLOCK_IDS.contains(
                "immersiveengineering:feedthrough"));
        assertTrue(SpecialShapeIntegration.BLOCK_IDS.contains(
                "immersiveengineering:fluid_pipe"));
    }

    @Test
    void conveyorStateChangesGeometryAndRotatesWithinOneBlock() {
        var flat = SpecialShapePlan.conveyor(
                "immersiveengineering:conveyor_basic", "north",
                new SpecialShapePlan.ConveyorState(0, null, 0, false, false)
        );
        var coveredSlope = SpecialShapePlan.conveyor(
                "immersiveengineering:conveyor_basic", "east",
                new SpecialShapePlan.ConveyorState(1, "minecraft:glass", 0, false, false)
        );

        assertFalse(flat.isEmpty());
        assertTrue(coveredSlope.size() > flat.size());
        coveredSlope.forEach(box -> {
            assertTrue(box.minX() >= 0F && box.minY() >= 0F && box.minZ() >= 0F);
            assertTrue(box.maxX() <= 1F && box.maxY() <= 1F && box.maxZ() <= 1F);
        });
    }

    @Test
    void pipeMaskFacesProduceCenterAndOneArmPerConnection() {
        assertEquals(4, SpecialShapePlan.pipe(Set.of(Face.DOWN, Face.NORTH, Face.EAST)).size());
    }
}
