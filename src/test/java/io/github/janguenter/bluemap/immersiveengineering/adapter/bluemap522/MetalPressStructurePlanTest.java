/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522.MetalPressStructurePlan.Action;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522.MetalPressStructurePlan.Entity;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522.MetalPressStructurePlan.Offset;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522.MetalPressStructurePlan.Snapshot;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetalPressStructurePlanTest {

    private static final Set<PartPosition> PARTS = Set.of(
            new PartPosition(0, -1, 0), new PartPosition(0, 0, 0),
            new PartPosition(0, 1, 0), new PartPosition(-1, -1, 0),
            new PartPosition(1, -1, 0), new PartPosition(-1, 0, 0),
            new PartPosition(1, 0, 0)
    );

    @Test
    void routesOnlyOneMasterForNormalMirroredAndEastStructures() {
        assertComplete("north", false);
        assertComplete("north", true);
        assertComplete("east", false);
    }

    @Test
    void oneMalformedDummyMakesEveryPartFallBackToStock() {
        Fixture fixture = fixture("north", false);
        Offset malformed = orient(new PartPosition(-1, 0, 0), false, "north");
        fixture.world().put(malformed, new Snapshot(
                "immersiveengineering:metal_press", properties("north", false, true),
                Entity.DUMMY, new PartPosition(99, 99, 99)
        ));

        fixture.world().forEach((position, snapshot) -> assertEquals(
                Action.STOCK,
                MetalPressStructurePlan.select(snapshot, PARTS, delta ->
                        fixture.world().get(add(position, delta)))
        ));
    }

    private static void assertComplete(String facing, boolean mirrored) {
        Fixture fixture = fixture(facing, mirrored);
        int masters = 0;
        int dummies = 0;
        for (Map.Entry<Offset, Snapshot> entry : fixture.world().entrySet()) {
            Offset position = entry.getKey();
            Action action = MetalPressStructurePlan.select(
                    entry.getValue(), PARTS, delta -> fixture.world().get(add(position, delta))
            );
            if (action == Action.EMIT_MASTER) {
                masters++;
            } else if (action == Action.SUPPRESS_DUMMY) {
                dummies++;
            }
        }
        assertEquals(1, masters);
        assertEquals(6, dummies);
    }

    private static Fixture fixture(String facing, boolean mirrored) {
        Map<Offset, Snapshot> world = new HashMap<>();
        for (PartPosition relative : PARTS) {
            boolean dummy = !relative.equals(new PartPosition(0, 0, 0));
            PartPosition absolute = new PartPosition(
                    relative.x() + 1, relative.y() + 1, relative.z()
            );
            world.put(orient(relative, mirrored, facing), new Snapshot(
                    "immersiveengineering:metal_press",
                    properties(facing, mirrored, dummy),
                    dummy ? Entity.DUMMY : Entity.MASTER,
                    dummy ? absolute : null
            ));
        }
        return new Fixture(world);
    }

    private static Map<String, String> properties(
            String facing,
            boolean mirrored,
            boolean dummy
    ) {
        return Map.of(
                "facing", facing,
                "mirrored", Boolean.toString(mirrored),
                "multiblockslave", Boolean.toString(dummy)
        );
    }

    private static Offset orient(PartPosition relative, boolean mirrored, String facing) {
        int x = mirrored ? -relative.x() : relative.x();
        return switch (facing) {
            case "north" -> new Offset(x, relative.y(), relative.z());
            case "east" -> new Offset(-relative.z(), relative.y(), x);
            default -> throw new IllegalArgumentException("unsupported test facing");
        };
    }

    private static Offset add(Offset first, Offset second) {
        return new Offset(
                first.x() + second.x(), first.y() + second.y(), first.z() + second.z()
        );
    }

    private record Fixture(Map<Offset, Snapshot> world) {
    }
}
