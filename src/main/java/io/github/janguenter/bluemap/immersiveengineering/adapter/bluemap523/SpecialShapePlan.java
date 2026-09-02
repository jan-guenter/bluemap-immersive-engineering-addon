/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Deterministic, data-only geometry plans for the bounded special-page families. */
final class SpecialShapePlan {

    static final String PIPE_ID = "immersiveengineering:fluid_pipe";
    static final String FEEDTHROUGH_ID = "immersiveengineering:feedthrough";

    private SpecialShapePlan() {
    }

    static List<Box> conveyor(String blockId, String facing, ConveyorState state) {
        Key accent = switch (blockId) {
            case "immersiveengineering:conveyor_dropper" -> SpecialShapeResources.DROPPER;
            case "immersiveengineering:conveyor_redstone" -> SpecialShapeResources.REDSTONE;
            case "immersiveengineering:conveyor_splitter" -> SpecialShapeResources.SPLITTER;
            case "immersiveengineering:conveyor_vertical" -> SpecialShapeResources.VERTICAL;
            case "immersiveengineering:conveyor_basic",
                    "immersiveengineering:conveyor_extract" -> SpecialShapeResources.CONVEYOR;
            default -> null;
        };
        if (accent == null || state == null || !horizontal(facing)
                || state.direction() < 0 || state.direction() > 2) {
            return List.of();
        }
        List<Box> local = new ArrayList<>();
        if (blockId.endsWith("conveyor_vertical")) {
            local.add(box(.08F, .05F, .78F, .92F, .95F, .91F, accent));
            local.add(box(.02F, .02F, .72F, .08F, .98F, .98F, SpecialShapeResources.CASING));
            local.add(box(.92F, .02F, .72F, .98F, .98F, .98F,
                    SpecialShapeResources.CASING));
        } else {
            addBelt(local, state.direction(), accent);
        }
        if (blockId.endsWith("conveyor_extract")) {
            float side = state.relativeExtractDir() == 2 ? .02F : .78F;
            local.add(box(side, .24F, .36F, side + .20F, .38F, .64F, accent));
        }
        if (blockId.endsWith("conveyor_redstone")) {
            float side = state.panelRight() ? .82F : .02F;
            local.add(box(side, .30F, .38F, side + .16F, .62F, .62F, accent));
        }
        if (blockId.endsWith("conveyor_splitter")) {
            float side = state.nextLeft() ? .02F : .78F;
            local.add(box(side, .24F, .42F, side + .20F, .36F, 1F, accent));
        }
        if (state.cover() != null && !state.cover().isBlank()) {
            local.add(box(.02F, .82F, .02F, .98F, .90F, .98F,
                    SpecialShapeResources.CASING));
        }
        return rotate(local, facing);
    }

    static List<Box> feedthrough(String facing, FeedthroughState state) {
        Key connector = state == null ? null
                : SpecialShapeResources.connectorTexture(state.wire());
        Key middle = state == null ? null
                : SpecialShapeResources.middleTexture(state.middleBlockId());
        if (connector == null || middle == null || !allDirections(facing)
                || state.offset() < -1 || state.offset() > 1) {
            return List.of();
        }
        List<Box> local = new ArrayList<>();
        if (state.offset() == 0) {
            local.add(box(0F, 0F, .34F, 1F, 1F, .66F, middle));
        }
        local.add(box(.43F, .43F, 0F, .57F, .57F, 1F, connector));
        local.add(box(.30F, .30F, .18F, .70F, .70F, .34F, connector));
        local.add(box(.30F, .30F, .66F, .70F, .70F, .82F, connector));
        return orient(local, facing);
    }

    static List<Box> pipe(Set<Face> faces) {
        List<Box> boxes = new ArrayList<>();
        boxes.add(box(.25F, .25F, .25F, .75F, .75F, .75F,
                SpecialShapeResources.FLUID_PIPE));
        for (Face face : faces) {
            boxes.add(switch (face) {
                case DOWN -> box(.25F, 0F, .25F, .75F, .25F, .75F,
                        SpecialShapeResources.FLUID_PIPE);
                case UP -> box(.25F, .75F, .25F, .75F, 1F, .75F,
                        SpecialShapeResources.FLUID_PIPE);
                case NORTH -> box(.25F, .25F, 0F, .75F, .75F, .25F,
                        SpecialShapeResources.FLUID_PIPE);
                case SOUTH -> box(.25F, .25F, .75F, .75F, .75F, 1F,
                        SpecialShapeResources.FLUID_PIPE);
                case WEST -> box(0F, .25F, .25F, .25F, .75F, .75F,
                        SpecialShapeResources.FLUID_PIPE);
                case EAST -> box(.75F, .25F, .25F, 1F, .75F, .75F,
                        SpecialShapeResources.FLUID_PIPE);
            });
        }
        return List.copyOf(boxes);
    }

    private static void addBelt(List<Box> boxes, int direction, Key accent) {
        for (int step = 0; step < 4; step++) {
            float z0 = step / 4F;
            float z1 = (step + 1) / 4F;
            float lift = direction == 0 ? 0F
                    : (direction == 1 ? step : 3 - step) * .1875F;
            boxes.add(box(.08F, .12F + lift, z0, .92F, .24F + lift, z1, accent));
            boxes.add(box(.02F, .12F + lift, z0, .08F, .36F + lift, z1,
                    SpecialShapeResources.CASING));
            boxes.add(box(.92F, .12F + lift, z0, .98F, .36F + lift, z1,
                    SpecialShapeResources.CASING));
        }
    }

    private static List<Box> rotate(List<Box> boxes, String facing) {
        int turns = switch (facing) {
            case "north" -> 0;
            case "east" -> 1;
            case "south" -> 2;
            case "west" -> 3;
            default -> -1;
        };
        if (turns < 0) {
            return List.of();
        }
        List<Box> result = new ArrayList<>();
        for (Box box : boxes) {
            Box rotated = box;
            for (int turn = 0; turn < turns; turn++) {
                rotated = new Box(
                        1F - rotated.maxZ(), rotated.minY(), rotated.minX(),
                        1F - rotated.minZ(), rotated.maxY(), rotated.maxX(),
                        rotated.texture()
                );
            }
            result.add(rotated);
        }
        return List.copyOf(result);
    }

    private static List<Box> orient(List<Box> boxes, String facing) {
        return switch (facing) {
            case "north" -> List.copyOf(boxes);
            case "east", "south", "west" -> rotate(boxes, facing);
            case "up" -> remap(boxes, 1);
            case "down" -> remap(boxes, -1);
            default -> List.of();
        };
    }

    private static List<Box> remap(List<Box> boxes, int up) {
        List<Box> result = new ArrayList<>();
        for (Box box : boxes) {
            if (up > 0) {
                result.add(new Box(
                        box.minX(), 1F - box.maxZ(), box.minY(),
                        box.maxX(), 1F - box.minZ(), box.maxY(), box.texture()
                ));
            } else {
                result.add(new Box(
                        box.minX(), box.minZ(), 1F - box.maxY(),
                        box.maxX(), box.maxZ(), 1F - box.minY(), box.texture()
                ));
            }
        }
        return List.copyOf(result);
    }

    private static boolean horizontal(String facing) {
        return "north".equals(facing) || "south".equals(facing)
                || "east".equals(facing) || "west".equals(facing);
    }

    private static boolean allDirections(String facing) {
        return horizontal(facing) || "up".equals(facing) || "down".equals(facing);
    }

    private static Box box(
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            Key texture
    ) {
        return new Box(minX, minY, minZ, maxX, maxY, maxZ, texture);
    }

    enum Face {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

    record Box(
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            Key texture
    ) {
    }

    record ConveyorState(
            int direction,
            String cover,
            int relativeExtractDir,
            boolean panelRight,
            boolean nextLeft
    ) {
    }

    record FeedthroughState(String wire, String middleBlockId, int offset) {
    }
}
