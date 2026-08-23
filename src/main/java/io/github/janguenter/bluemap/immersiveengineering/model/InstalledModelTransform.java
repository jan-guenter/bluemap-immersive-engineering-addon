/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Vertex;

/** Applies the exact blockstate rotations used by installed IE OBJ models. */
public final class InstalledModelTransform {

    private static final String CONNECTOR_PREFIX = "immersiveengineering:connector_";
    private static final String TESLA_COIL = "immersiveengineering:tesla_coil";

    private InstalledModelTransform() {
    }

    public static Triangle triangle(
            Triangle input,
            String blockId,
            boolean mirrored,
            String facing
    ) {
        if (!blockId.startsWith(CONNECTOR_PREFIX) && !TESLA_COIL.equals(blockId)) {
            return MetalPressTransform.triangle(input, mirrored, facing);
        }
        Rotation rotation = blockId.startsWith(CONNECTOR_PREFIX)
                ? connectorRotation(facing) : teslaRotation(facing);
        Vertex first = vertex(input.first(), mirrored, rotation);
        Vertex second = vertex(input.second(), mirrored, rotation);
        Vertex third = vertex(input.third(), mirrored, rotation);
        return mirrored
                ? new Triangle(first, third, second, input.material())
                : new Triangle(first, second, third, input.material());
    }

    private static Vertex vertex(Vertex input, boolean mirrored, Rotation rotation) {
        float x = mirrored ? 1F - input.x() : input.x();
        float y = input.y();
        float z = input.z();
        float afterXy;
        float afterXz;
        switch (rotation.x()) {
            case 0 -> {
                afterXy = y;
                afterXz = z;
            }
            case 90 -> {
                afterXy = 1F - z;
                afterXz = y;
            }
            case 180 -> {
                afterXy = 1F - y;
                afterXz = 1F - z;
            }
            case 270 -> {
                afterXy = z;
                afterXz = 1F - y;
            }
            default -> throw new IllegalArgumentException("unsupported installed-model X rotation");
        }
        return switch (rotation.y()) {
            case 0 -> new Vertex(x, afterXy, afterXz, input.u(), input.v());
            case 90 -> new Vertex(1F - afterXz, afterXy, x, input.u(), input.v());
            case 180 -> new Vertex(1F - x, afterXy, 1F - afterXz, input.u(), input.v());
            case 270 -> new Vertex(afterXz, afterXy, 1F - x, input.u(), input.v());
            default -> throw new IllegalArgumentException("unsupported installed-model Y rotation");
        };
    }

    private static Rotation connectorRotation(String facing) {
        return switch (facing) {
            case "down" -> new Rotation(0, 0);
            case "up" -> new Rotation(180, 0);
            case "south" -> new Rotation(90, 0);
            case "west" -> new Rotation(90, 90);
            case "north" -> new Rotation(90, 180);
            case "east" -> new Rotation(90, 270);
            default -> throw new IllegalArgumentException("unsupported connector facing");
        };
    }

    private static Rotation teslaRotation(String facing) {
        return switch (facing) {
            case "north" -> new Rotation(0, 0);
            case "east" -> new Rotation(0, 90);
            case "south" -> new Rotation(0, 180);
            case "west" -> new Rotation(0, 270);
            case "down" -> new Rotation(90, 0);
            case "up" -> new Rotation(270, 0);
            default -> throw new IllegalArgumentException("unsupported Tesla Coil facing");
        };
    }

    private record Rotation(int x, int y) {
    }
}
