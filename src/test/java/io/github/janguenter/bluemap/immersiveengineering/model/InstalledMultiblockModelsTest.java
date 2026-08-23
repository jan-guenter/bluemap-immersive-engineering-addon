/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

class InstalledMultiblockModelsTest {

    @Test
    void loadsEveryStaticGalleryMultiblockFromExactArtifact() throws Exception {
        String property = System.getProperty("immersiveEngineeringJar");
        assumeTrue(property != null && !property.isBlank());

        Map<String, InstalledMultiblockModel> models =
                InstalledMultiblockModels.load(Path.of(property));

        assertEquals(24, models.size());
        models.values().forEach(model -> {
            assertFalse(model.model().triangles().isEmpty());
            assertFalse(model.materials().isEmpty());
            assertFalse(model.parts().isEmpty());
        });
    }
}
