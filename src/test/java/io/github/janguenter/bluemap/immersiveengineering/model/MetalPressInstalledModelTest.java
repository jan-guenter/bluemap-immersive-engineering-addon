/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetalPressInstalledModelTest {

    @Test
    void exactInstalledNormalAndMirroredResourcesCompile() throws IOException {
        String property = System.getProperty("immersiveEngineeringJar");
        Assumptions.assumeTrue(property != null && Files.isRegularFile(Path.of(property)));

        MetalPressInstalledModel installed = MetalPressInstalledModel.load(Path.of(property));

        assertEquals(272, installed.model().triangles().size());
        assertEquals(Set.of("metalPress", "conveyor"), installed.materials().keySet());
        assertEquals(Set.of(
                new PartPosition(0, -1, 0), new PartPosition(0, 0, 0),
                new PartPosition(0, 1, 0), new PartPosition(-1, -1, 0),
                new PartPosition(1, -1, 0), new PartPosition(-1, 0, 0),
                new PartPosition(1, 0, 0)
        ), installed.parts());
    }
}
