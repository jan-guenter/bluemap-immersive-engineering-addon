/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class InstalledSpecialModelsTest {

    private static final Set<String> EXPECTED_IDS = Set.of(
            id("connector_lv"), id("connector_lv_relay"),
            id("connector_mv"), id("connector_mv_relay"),
            id("connector_hv"), id("connector_hv_relay"),
            id("connector_redstone"), id("connector_structural"),
            id("charging_station"), id("sample_drill"), id("tesla_coil"),
            id("turret_chem"), id("turret_gun"), id("cloche"),
            id("blastfurnace_preheater"), id("watermill"), id("windmill"),
            id("workbench"), id("coresample")
    );
    private static final Map<String, Integer> PART_COUNTS = Map.ofEntries(
            Map.entry(id("connector_lv"), 1),
            Map.entry(id("connector_lv_relay"), 1),
            Map.entry(id("connector_mv"), 1),
            Map.entry(id("connector_mv_relay"), 1),
            Map.entry(id("connector_hv"), 1),
            Map.entry(id("connector_hv_relay"), 1),
            Map.entry(id("connector_redstone"), 1),
            Map.entry(id("connector_structural"), 1),
            Map.entry(id("charging_station"), 1),
            Map.entry(id("sample_drill"), 3),
            Map.entry(id("tesla_coil"), 2),
            Map.entry(id("turret_chem"), 2),
            Map.entry(id("turret_gun"), 2),
            Map.entry(id("cloche"), 3),
            Map.entry(id("blastfurnace_preheater"), 3),
            Map.entry(id("watermill"), 1),
            Map.entry(id("windmill"), 1),
            Map.entry(id("workbench"), 2),
            Map.entry(id("coresample"), 1)
    );

    @Test
    void advertisesExactSpecialBlockRosterWithoutOpeningArtifact() {
        assertEquals(EXPECTED_IDS, InstalledSpecialModels.blockIds());
    }

    @Test
    void loadsEverySpecialModelFromExactArtifact() throws Exception {
        String property = System.getProperty("immersiveEngineeringJar");
        assumeTrue(property != null && !property.isBlank());

        Map<String, InstalledMultiblockModel> models =
                InstalledSpecialModels.load(Path.of(property));

        assertEquals(EXPECTED_IDS, models.keySet());
        models.forEach((id, model) -> {
            assertEquals(id, model.blockId());
            assertEquals(PART_COUNTS.get(id), model.parts().size());
            assertFalse(model.model().triangles().isEmpty());
            assertFalse(model.materials().isEmpty());
            Set<String> referenced = model.model().triangles().stream()
                    .map(WavefrontModel.Triangle::material)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            assertEquals(referenced, model.materials().keySet());
        });
    }

    @Test
    void resolvesRelayAliasesAndCompositeAndDynamicModels() throws Exception {
        String property = System.getProperty("immersiveEngineeringJar");
        assumeTrue(property != null && !property.isBlank());

        Map<String, InstalledMultiblockModel> models =
                InstalledSpecialModels.load(Path.of(property));

        assertEquals(
                Key.parse("immersiveengineering:block/connector/relay_lv"),
                models.get(id("connector_lv_relay")).materials().get("connectorLV")
        );
        assertEquals(
                Key.parse("immersiveengineering:block/connector/relay_mv"),
                models.get(id("connector_mv_relay")).materials().get("connectorMV")
        );
        assertTrue(models.get(id("charging_station")).model().triangles().size()
                > models.get(id("connector_lv")).model().triangles().size());
        assertTrue(models.get(id("windmill")).model().triangles().size() > 100);
        assertEquals(
                Set.of("cloche", "farmland"),
                models.get(id("cloche")).materials().keySet()
        );
    }

    private static String id(String path) {
        return "immersiveengineering:" + path;
    }
}
