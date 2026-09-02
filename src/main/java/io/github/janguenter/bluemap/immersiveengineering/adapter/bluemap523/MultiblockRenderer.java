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
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModel;

import java.util.Map;

/** Static whole-model renderer for admitted formed and special IE blocks. */
final class MultiblockRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer stock;
    private final InstalledModelEmitter emitter;
    private final AddonRuntime runtime;
    private final Map<String, InstalledMultiblockModel> installed;

    MultiblockRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.stock = new ResourceModelRenderer(resourcePack, textures, settings);
        this.emitter = new InstalledModelEmitter(resourcePack, textures, settings);
        this.runtime = runtime;
        this.installed = MultiblockPackData.get(resourcePack);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant ignored,
            TileModelView target,
            Color mapColor
    ) {
        InstalledMultiblockModel model = installed == null ? null : installed.get(
                block.getBlockState().getId().getFormatted()
        );
        if (model == null) {
            renderStock(block, target, mapColor);
            return;
        }
        Map<String, String> properties = block.getBlockState().getProperties();
        String slave = properties.get("multiblockslave");
        if ("true".equals(slave)) {
            return;
        }
        String facing = properties.getOrDefault("facing", "north");
        boolean mirrored = Boolean.parseBoolean(properties.getOrDefault("mirrored", "false"));
        int start = target.getStart();
        Color initial = new Color().set(mapColor);
        try {
            if (!emitter.emit(model, mirrored, facing, block, target, mapColor)) {
                reset(target, start);
                mapColor.set(initial);
                renderStock(block, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            reset(target, start);
            mapColor.set(initial);
            runtime.inactive("multiblock-renderer-" + exception.getClass().getSimpleName());
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

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }
}
