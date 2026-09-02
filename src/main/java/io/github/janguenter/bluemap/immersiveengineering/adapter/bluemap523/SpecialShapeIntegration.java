/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;

import java.util.List;
import java.util.Set;

/** Public, side-effect-free registration contract for the bounded special renderer. */
public final class SpecialShapeIntegration {

    public static final Key RENDERER_KEY =
            Key.parse("bluemap_immersive_engineering:special_shape");
    public static final Key SYNTHETIC_BLOCK_STATE =
            Key.parse("bluemap_immersive_engineering:special_shape");
    public static final Set<String> CONVEYOR_BLOCK_IDS = Set.of(
            "immersiveengineering:conveyor_basic",
            "immersiveengineering:conveyor_dropper",
            "immersiveengineering:conveyor_extract",
            "immersiveengineering:conveyor_redstone",
            "immersiveengineering:conveyor_splitter",
            "immersiveengineering:conveyor_vertical"
    );
    public static final Set<String> BLOCK_IDS = Set.of(
            "immersiveengineering:conveyor_basic",
            "immersiveengineering:conveyor_dropper",
            "immersiveengineering:conveyor_extract",
            "immersiveengineering:conveyor_redstone",
            "immersiveengineering:conveyor_splitter",
            "immersiveengineering:conveyor_vertical",
            "immersiveengineering:feedthrough",
            "immersiveengineering:fluid_pipe"
    );

    private static final List<BlockEntityType> BLOCK_ENTITY_TYPES = List.of(
            conveyorType("basic"), conveyorType("dropper"), conveyorType("extract"),
            conveyorType("redstone"), conveyorType("splitter"), conveyorType("vertical"),
            type("feedthrough", FeedthroughBlockEntityData.class),
            type("fluidpipe", FluidPipeBlockEntityData.class)
    );

    private SpecialShapeIntegration() {
    }

    /** Creates the one renderer-type instance that the adapter must register and retain. */
    public static BlockRendererType createRendererType() {
        return new BlockRendererType.Impl(
                RENDERER_KEY,
                SpecialShapeRenderer::new
        );
    }

    /** Exact observed IE block-entity registrations required for persisted special state. */
    public static List<BlockEntityType> blockEntityTypes() {
        return BLOCK_ENTITY_TYPES;
    }

    /** True only for the eight gallery block IDs owned by this subsystem. */
    public static boolean covers(Key blockId) {
        return BLOCK_IDS.contains(blockId.getFormatted());
    }

    /** Texture keys the profile extension must collect and validate before routing. */
    public static Set<Key> requiredTextureKeys() {
        return SpecialShapeResources.requiredTextureKeys();
    }

    private static BlockEntityType conveyorType(String id) {
        return type(id, ConveyorBlockEntityData.class);
    }

    private static BlockEntityType type(
            String id,
            Class<? extends de.bluecolored.bluemap.core.world.BlockEntity> data
    ) {
        return new BlockEntityType.Impl(Key.parse("immersiveengineering:" + id), data);
    }
}
