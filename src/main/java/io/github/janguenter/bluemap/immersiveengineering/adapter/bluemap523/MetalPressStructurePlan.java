/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** All-or-stock classification for one complete seven-part Metal Press. */
final class MetalPressStructurePlan {

    private static final String BLOCK = "immersiveengineering:metal_press";
    private static final PartPosition MASTER_IN_TEMPLATE = new PartPosition(1, 1, 0);

    private MetalPressStructurePlan() {
    }

    static Action select(
            Snapshot current,
            Set<PartPosition> modelParts,
            Function<Offset, Snapshot> lookup
    ) {
        State state = state(current);
        if (state == null) {
            return Action.STOCK;
        }
        PartPosition currentPart = part(current, state);
        if (currentPart == null) {
            return Action.STOCK;
        }
        PartPosition relative = relative(currentPart);
        Offset fromMaster = orient(relative, state.mirrored(), state.facing());
        Snapshot master = lookup.apply(fromMaster.negate());
        if (!matches(master, state, MASTER_IN_TEMPLATE, false)) {
            return Action.STOCK;
        }
        for (PartPosition modelPart : modelParts) {
            Offset offset = orient(modelPart, state.mirrored(), state.facing());
            Snapshot found = lookup.apply(offset.subtract(fromMaster));
            PartPosition templatePart = absolute(modelPart);
            if (!matches(found, state, templatePart,
                    !modelPart.equals(new PartPosition(0, 0, 0)))) {
                return Action.STOCK;
            }
        }
        return relative.equals(new PartPosition(0, 0, 0))
                ? Action.EMIT_MASTER : Action.SUPPRESS_DUMMY;
    }

    private static boolean matches(
            Snapshot snapshot,
            State expected,
            PartPosition position,
            boolean dummy
    ) {
        State state = state(snapshot);
        return state != null
                && state.facing().equals(expected.facing())
                && state.mirrored() == expected.mirrored()
                && state.dummy() == dummy
                && position.equals(part(snapshot, state));
    }

    private static State state(Snapshot snapshot) {
        if (snapshot == null || !BLOCK.equals(snapshot.blockId())) {
            return null;
        }
        Map<String, String> properties = snapshot.properties();
        if (properties == null || properties.size() != 3
                || !Set.of("facing", "mirrored", "multiblockslave")
                .equals(properties.keySet())) {
            return null;
        }
        String facing = properties.get("facing");
        if (!Set.of("north", "east", "south", "west").contains(facing)) {
            return null;
        }
        String mirrored = properties.get("mirrored");
        String slave = properties.get("multiblockslave");
        if (!("true".equals(mirrored) || "false".equals(mirrored))
                || !("true".equals(slave) || "false".equals(slave))) {
            return null;
        }
        return new State(facing, Boolean.parseBoolean(mirrored), Boolean.parseBoolean(slave));
    }

    private static PartPosition part(Snapshot snapshot, State state) {
        if (state.dummy()) {
            return snapshot.entity() == Entity.DUMMY ? snapshot.position() : null;
        }
        return snapshot.entity() == Entity.MASTER ? MASTER_IN_TEMPLATE : null;
    }

    private static PartPosition relative(PartPosition position) {
        return new PartPosition(
                position.x() - MASTER_IN_TEMPLATE.x(),
                position.y() - MASTER_IN_TEMPLATE.y(),
                position.z() - MASTER_IN_TEMPLATE.z()
        );
    }

    private static PartPosition absolute(PartPosition relative) {
        return new PartPosition(
                relative.x() + MASTER_IN_TEMPLATE.x(),
                relative.y() + MASTER_IN_TEMPLATE.y(),
                relative.z() + MASTER_IN_TEMPLATE.z()
        );
    }

    private static Offset orient(PartPosition relative, boolean mirrored, String facing) {
        int x = mirrored ? -relative.x() : relative.x();
        int z = relative.z();
        return switch (facing) {
            case "north" -> new Offset(x, relative.y(), z);
            case "east" -> new Offset(-z, relative.y(), x);
            case "south" -> new Offset(-x, relative.y(), -z);
            case "west" -> new Offset(z, relative.y(), -x);
            default -> throw new IllegalArgumentException("unsupported Metal Press facing");
        };
    }

    enum Action {
        EMIT_MASTER,
        SUPPRESS_DUMMY,
        STOCK
    }

    enum Entity {
        MASTER,
        DUMMY,
        OTHER
    }

    record Snapshot(
            String blockId,
            Map<String, String> properties,
            Entity entity,
            PartPosition position
    ) {
        Snapshot {
            properties = properties == null ? null : Map.copyOf(properties);
        }
    }

    record Offset(int x, int y, int z) {
        Offset subtract(Offset other) {
            return new Offset(x - other.x, y - other.y, z - other.z);
        }

        Offset negate() {
            return new Offset(-x, -y, -z);
        }
    }

    private record State(String facing, boolean mirrored, boolean dummy) {
    }
}
