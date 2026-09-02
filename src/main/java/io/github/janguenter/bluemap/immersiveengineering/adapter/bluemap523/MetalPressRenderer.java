/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523.MetalPressStructurePlan.Action;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523.MetalPressStructurePlan.Entity;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523.MetalPressStructurePlan.Snapshot;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;

/** Whole-model master renderer with all-or-stock dummy routing. */
final class MetalPressRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer stock;
    private final MetalPressEmitter emitter;
    private final AddonRuntime runtime;
    private final MetalPressInstalledModel installed;

    MetalPressRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.stock = new ResourceModelRenderer(resourcePack, textures, settings);
        this.emitter = new MetalPressEmitter(resourcePack, textures, settings);
        this.runtime = runtime;
        this.installed = MetalPressPackData.get(resourcePack);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant ignored,
            TileModelView target,
            Color mapColor
    ) {
        if (installed == null) {
            renderStock(block, target, mapColor);
            return;
        }
        Action action = MetalPressStructurePlan.select(
                snapshot(block), installed.parts(), offset -> snapshot(
                        block.getNeighborBlock(offset.x(), offset.y(), offset.z())
                )
        );
        if (action == Action.SUPPRESS_DUMMY) {
            return;
        }
        if (action == Action.STOCK) {
            renderStock(block, target, mapColor);
            return;
        }
        int start = target.getStart();
        Color initial = new Color().set(mapColor);
        try {
            String facing = block.getBlockState().getProperties().get("facing");
            boolean mirrored = Boolean.parseBoolean(
                    block.getBlockState().getProperties().get("mirrored")
            );
            if (!emitter.emit(installed, mirrored, facing, block, target, mapColor)) {
                reset(target, start);
                mapColor.set(initial);
                renderStock(block, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            reset(target, start);
            mapColor.set(initial);
            runtime.inactive("metal-press-renderer-" + exception.getClass().getSimpleName());
            renderStock(block, target, mapColor);
        }
    }

    private void renderStock(BlockNeighborhood block, TileModelView target, Color mapColor) {
        var state = resourcePack.getBlockStates().get(block.getBlockState().getId());
        if (state == null) {
            return;
        }
        state.forEach(
                block.getBlockState(), block.getX(), block.getY(), block.getZ(),
                variant -> stock.render(block, variant, target, mapColor)
        );
    }

    private static Snapshot snapshot(ExtendedBlock block) {
        Object entity = block.getBlockEntity();
        Entity type = entity instanceof MetalPressMasterBlockEntityData
                ? Entity.MASTER : entity instanceof MetalPressDummyBlockEntityData
                ? Entity.DUMMY : Entity.OTHER;
        PartPosition position = entity instanceof MetalPressDummyBlockEntityData dummy
                ? dummy.position() : null;
        return new Snapshot(
                block.getBlockState().getId().getFormatted(),
                block.getBlockState().getProperties(), type, position
        );
    }

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }
}
