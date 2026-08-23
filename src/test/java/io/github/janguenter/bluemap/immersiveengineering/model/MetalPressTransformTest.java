/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Vertex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetalPressTransformTest {

    @Test
    void mirrorsBeforeRotatingAboutBlockCenterAndReversesWinding() {
        Vertex a = new Vertex(-1F, 0F, 0F, 0F, 0F);
        Vertex b = new Vertex(0F, 0F, 0F, 0.5F, 0F);
        Vertex c = new Vertex(0F, 1F, 1F, 0.5F, 1F);
        Triangle transformed = MetalPressTransform.triangle(
                new Triangle(a, b, c, "press"), true, "east"
        );

        assertEquals(new Vertex(1F, 0F, 2F, 0F, 0F), transformed.first());
        assertEquals(new Vertex(0F, 1F, 1F, 0.5F, 1F), transformed.second());
        assertEquals(new Vertex(1F, 0F, 1F, 0.5F, 0F), transformed.third());
    }
}
