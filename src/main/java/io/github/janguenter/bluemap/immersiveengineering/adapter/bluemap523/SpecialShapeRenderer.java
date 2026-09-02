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
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523.SpecialShapePlan.Box;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523.SpecialShapePlan.Face;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Renderer for the eight exact special-page block IDs. */
final class SpecialShapeRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer stock;
    private final SpecialShapeEmitter emitter;

    SpecialShapeRenderer(ResourcePack resourcePack, TextureGallery textures, RenderSettings settings) {
        this.resourcePack = resourcePack;
        this.stock = new ResourceModelRenderer(resourcePack, textures, settings);
        this.emitter = new SpecialShapeEmitter(resourcePack, textures, settings);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant ignored,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        Color initial = new Color().set(mapColor);
        try {
            List<Box> plan = select(block);
            if (!emitter.emit(plan, block, target, mapColor)) {
                reset(target, start);
                mapColor.set(initial);
                renderStock(block, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            reset(target, start);
            mapColor.set(initial);
            renderStock(block, target, mapColor);
        }
    }

    private static List<Box> select(BlockNeighborhood block) {
        String id = block.getBlockState().getId().getFormatted();
        String facing = block.getBlockState().getProperties().get("facing");
        Object entity = block.getBlockEntity();
        if (SpecialShapeIntegration.CONVEYOR_BLOCK_IDS.contains(id)) {
            ConveyorBlockEntityData data = entity instanceof ConveyorBlockEntityData conveyor
                    ? conveyor : null;
            return SpecialShapePlan.conveyor(id, facing, data == null ? null : data.state());
        }
        if (SpecialShapePlan.FEEDTHROUGH_ID.equals(id)) {
            FeedthroughBlockEntityData data = entity instanceof FeedthroughBlockEntityData feedthrough
                    ? feedthrough : null;
            return SpecialShapePlan.feedthrough(facing, data == null ? null : data.state());
        }
        if (SpecialShapePlan.PIPE_ID.equals(id)) {
            Set<Face> faces = entity instanceof FluidPipeBlockEntityData pipe
                    ? pipe.connectedFaces() : neighborPipeFaces(block);
            return SpecialShapePlan.pipe(faces);
        }
        return List.of();
    }

    private static Set<Face> neighborPipeFaces(BlockNeighborhood block) {
        EnumSet<Face> faces = EnumSet.noneOf(Face.class);
        Face[] order = Face.values();
        int[][] offsets = {
                {0, -1, 0}, {0, 1, 0}, {0, 0, -1},
                {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
        };
        for (int index = 0; index < offsets.length; index++) {
            int[] offset = offsets[index];
            String id = block.getNeighborBlock(offset[0], offset[1], offset[2])
                    .getBlockState().getId().getFormatted();
            if (SpecialShapePlan.PIPE_ID.equals(id)) {
                faces.add(order[index]);
            }
        }
        return faces;
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
