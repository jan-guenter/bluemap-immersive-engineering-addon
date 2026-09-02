/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.RenderPassType;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
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

    private BlueMap523Adapter() {
    }

    /** Registers exact admission, formed multiblock routing, and audited DTOs. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(BlockRendererType.REGISTRY, SPECIAL_RENDERER)
                || !RegistryGuard.canRegister(RenderPassType.REGISTRY, WIRE_RENDER_PASS)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || BLOCK_ENTITIES.stream().anyMatch(type ->
                !RegistryGuard.canRegister(BlockEntityType.REGISTRY, type))) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(BlockRendererType.REGISTRY, SPECIAL_RENDERER)
                || !RegistryGuard.register(RenderPassType.REGISTRY, WIRE_RENDER_PASS)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        for (BlockEntityType type : BLOCK_ENTITIES) {
            if (!RegistryGuard.register(BlockEntityType.REGISTRY, type)) {
                RUNTIME.fail("block-entity-registry-collision");
                return false;
            }
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
}
