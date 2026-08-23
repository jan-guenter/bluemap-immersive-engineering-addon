/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Vertex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstalledModelTransformTest {

    private static final Triangle POINTS = new Triangle(
            new Vertex(0F, 0F, 0F, 0F, 0F),
            new Vertex(1F, 0F, 0F, 1F, 0F),
            new Vertex(0F, 1F, 0F, 0F, 1F),
            "material"
    );

    @Test
    void connectorDownIsItsInstalledObjOrientation() {
        assertEquals(POINTS, InstalledModelTransform.triangle(
                POINTS, "immersiveengineering:connector_lv", false, "down"
        ));
    }

    @Test
    void connectorNorthUsesItsBlockstateXAndYRotations() {
        Triangle result = InstalledModelTransform.triangle(
                POINTS, "immersiveengineering:connector_lv", false, "north"
        );
        assertEquals(new Vertex(1F, 1F, 1F, 0F, 0F), result.first());
        assertEquals(new Vertex(0F, 1F, 1F, 1F, 0F), result.second());
        assertEquals(new Vertex(1F, 1F, 0F, 0F, 1F), result.third());
    }

    @Test
    void teslaUpUsesItsNegativeQuarterTurn() {
        Triangle result = InstalledModelTransform.triangle(
                POINTS, "immersiveengineering:tesla_coil", false, "up"
        );
        assertEquals(new Vertex(0F, 0F, 1F, 0F, 0F), result.first());
        assertEquals(new Vertex(1F, 0F, 1F, 1F, 0F), result.second());
        assertEquals(new Vertex(0F, 0F, 0F, 0F, 1F), result.third());
    }
}
