/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import java.util.List;

/** Immutable textured triangles read from the installed Immersive Engineering OBJ. */
public record WavefrontModel(List<Triangle> triangles) {

    public WavefrontModel {
        triangles = List.copyOf(triangles);
    }

    /** One triangle and its MTL material name. */
    public record Triangle(Vertex first, Vertex second, Vertex third, String material) {
    }

    /** Model-space position and normalized texture coordinates. */
    public record Vertex(float x, float y, float z, float u, float v) {
    }
}
