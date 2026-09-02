/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

class RegistrationPlanContractTest {

    @Test
    void preservesPlanSeparationRegistrationOrderAndFailurePolicy() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/io/github/janguenter/bluemap/immersiveengineering/adapter/bluemap523/BlueMap523Adapter.java"
        ));
        int renderer = source.indexOf(".add(BlockRendererType.REGISTRY, RENDERER)");
        int special = source.indexOf(".add(BlockRendererType.REGISTRY, SPECIAL_RENDERER)");
        int pass = source.indexOf(".add(RenderPassType.REGISTRY, WIRE_RENDER_PASS)");
        int extension = source.indexOf(".add(ResourcePack.Extension.REGISTRY, EXTENSION)");
        int preflight = source.indexOf(
                "if (!BASE_REGISTRATIONS.canApply() || !BLOCK_ENTITY_REGISTRATIONS.canApply())"
        );
        int collision = source.indexOf("RUNTIME.fail(\"registry-collision\")");
        int baseApply = source.indexOf("if (!BASE_REGISTRATIONS.apply())");
        int baseFailure = source.indexOf("RUNTIME.fail(\"registry-registration-failed\")");
        int blockApply = source.indexOf("if (!BLOCK_ENTITY_REGISTRATIONS.apply())");
        int blockFailure = source.indexOf("RUNTIME.fail(\"block-entity-registry-collision\")");

        assertTrue(renderer >= 0 && renderer < special && special < pass && pass < extension);
        assertTrue(preflight >= 0 && preflight < collision);
        assertTrue(baseApply > collision && baseApply < baseFailure);
        assertTrue(blockApply > baseFailure && blockApply < blockFailure);
        assertTrue(source.contains("plan = plan.add(BlockEntityType.REGISTRY, type)"));
    }
}
