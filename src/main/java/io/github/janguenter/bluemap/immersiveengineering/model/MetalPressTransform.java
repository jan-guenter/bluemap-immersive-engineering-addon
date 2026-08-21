/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Vertex;

/** Exact IE mirror followed by horizontal facing rotation about block center. */
public final class MetalPressTransform {

    private MetalPressTransform() {
    }

    public static Triangle triangle(Triangle input, boolean mirrored, String facing) {
        Vertex first = vertex(input.first(), mirrored, facing);
        Vertex second = vertex(input.second(), mirrored, facing);
        Vertex third = vertex(input.third(), mirrored, facing);
        return mirrored
                ? new Triangle(first, third, second, input.material())
                : new Triangle(first, second, third, input.material());
    }

    public static Vertex vertex(Vertex input, boolean mirrored, String facing) {
        float x = mirrored ? 1F - input.x() : input.x();
        float z = input.z();
        return switch (facing) {
            case "north" -> new Vertex(x, input.y(), z, input.u(), input.v());
            case "east" -> new Vertex(1F - z, input.y(), x, input.u(), input.v());
            case "south" -> new Vertex(1F - x, input.y(), 1F - z, input.u(), input.v());
            case "west" -> new Vertex(z, input.y(), 1F - x, input.u(), input.v());
            default -> throw new IllegalArgumentException("unsupported Metal Press facing");
        };
    }
}
