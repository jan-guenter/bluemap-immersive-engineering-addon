/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialShapeResourcesTest {

    @Test
    void exactArtifactContainsEveryIeTextureUsedByStaticGeometry() throws IOException {
        String property = System.getProperty("immersiveEngineeringJar");
        Assumptions.assumeTrue(property != null && Files.isRegularFile(Path.of(property)));

        assertDoesNotThrow(() -> SpecialShapeResources.verifyExactJar(Path.of(property)));
        assertEquals(15, SpecialShapeResources.requiredTextureKeys().size());
    }
}
