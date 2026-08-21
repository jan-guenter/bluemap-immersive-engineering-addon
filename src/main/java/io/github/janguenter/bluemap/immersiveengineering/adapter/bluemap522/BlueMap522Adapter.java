/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;

import java.util.List;

/** BlueMap 5.22 registration boundary. Family renderer registrations go here. */
public final class BlueMap522Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_immersive_engineering:metal_press"),
            (pack, textures, settings) -> new MetalPressRenderer(
                    pack, textures, settings, RUNTIME
            )
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ProfileResourceExtensionType(RUNTIME);
    private static final List<BlockEntityType> BLOCK_ENTITIES = List.of(
            new BlockEntityType.Impl(
                    Key.parse("immersiveengineering:metal_press_master"),
                    MetalPressMasterBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("immersiveengineering:metal_press_dummy"),
                    MetalPressDummyBlockEntityData.class
            )
    );

    private BlueMap522Adapter() {
    }

    /** Registers exact admission, Metal Press routing, and its two BlueNBT DTOs. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || BLOCK_ENTITIES.stream().anyMatch(type ->
                !RegistryGuard.canRegister(BlockEntityType.REGISTRY, type))) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
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

    static boolean isExpectedDispatch(Variant variant) {
        return variant != null
                && variant.getRenderer() == RENDERER
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }
}
