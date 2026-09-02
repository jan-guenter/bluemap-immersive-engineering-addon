/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.RenderPassType;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistrationPlan;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;

import java.util.ArrayList;
import java.util.List;

/** BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_immersive_engineering:formed_multiblock"),
            (pack, textures, settings) -> new MultiblockRenderer(
                    pack, textures, settings, RUNTIME
            )
    );
    static final BlockRendererType SPECIAL_RENDERER =
            SpecialShapeIntegration.createRendererType();
    private static final RenderPassType WIRE_RENDER_PASS = new RenderPassType.Impl(
            Key.parse("bluemap_immersive_engineering:wires"),
            (pack, textures, settings) -> new WireRenderPass(pack, textures, RUNTIME)
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    Key.parse("bluemap_immersive_engineering:exact_profile"),
                    pack -> new ProfileResourceExtension(pack, RUNTIME)
            );
    private static final List<BlockEntityType> BLOCK_ENTITIES = blockEntityTypes();
    private static final RegistrationPlan BASE_REGISTRATIONS = RegistrationPlan.empty()
            .add(BlockRendererType.REGISTRY, RENDERER)
            .add(BlockRendererType.REGISTRY, SPECIAL_RENDERER)
            .add(RenderPassType.REGISTRY, WIRE_RENDER_PASS)
            .add(ResourcePack.Extension.REGISTRY, EXTENSION);
    private static final RegistrationPlan BLOCK_ENTITY_REGISTRATIONS = blockEntityPlan();

    private BlueMap523Adapter() {
    }

    /** Registers exact admission, formed multiblock routing, and audited DTOs. */
    public static synchronized boolean install() {
        if (!BASE_REGISTRATIONS.canApply() || !BLOCK_ENTITY_REGISTRATIONS.canApply()) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!BASE_REGISTRATIONS.apply()) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        if (!BLOCK_ENTITY_REGISTRATIONS.apply()) {
            RUNTIME.fail("block-entity-registry-collision");
            return false;
        }
        return true;
    }

    private static List<BlockEntityType> blockEntityTypes() {
        List<BlockEntityType> result = new ArrayList<>();
        result.add(new BlockEntityType.Impl(
                Key.parse("immersiveengineering:metal_press_master"),
                MetalPressMasterBlockEntityData.class
        ));
        result.add(new BlockEntityType.Impl(
                Key.parse("immersiveengineering:metal_press_dummy"),
                MetalPressDummyBlockEntityData.class
        ));
        result.addAll(SpecialShapeIntegration.blockEntityTypes());
        return List.copyOf(result);
    }

    private static RegistrationPlan blockEntityPlan() {
        RegistrationPlan plan = RegistrationPlan.empty();
        for (BlockEntityType type : BLOCK_ENTITIES) {
            plan = plan.add(BlockEntityType.REGISTRY, type);
        }
        return plan;
    }
}
