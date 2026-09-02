/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.TileMetaConsumer;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderPass;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.datapack.DataPack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.block.Block;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;

import java.util.List;

/** Tile render pass for world-level IE wire-network saved data. */
final class WireRenderPass implements RenderPass {

    private final AddonRuntime runtime;
    private final WireAttachmentReader reader = new WireAttachmentReader();
    private final WireEmitter emitter;

    WireRenderPass(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            AddonRuntime runtime
    ) {
        this.runtime = runtime;
        this.emitter = createEmitter(resourcePack, textureGallery);
    }

    @Override
    public void render(
            World world,
            Vector3i modelMin,
            Vector3i modelMax,
            Vector3i modelAnchor,
            TileModelView tileModel,
            TileMetaConsumer tileMetaConsumer
    ) {
        if (!runtime.active() || emitter == null || !(world instanceof MCAWorld mcaWorld)
                || !DataPack.DIMENSION_OVERWORLD.equals(mcaWorld.getDimension())) {
            return;
        }
        int passStart = tileModel.getTileModel().size();
        try {
            List<WireSpan> spans = reader.read(mcaWorld.getWorldFolder());
            Block lightSample = new Block(world, 0, 0, 0);
            for (WireSpan span : spans) {
                WireSpan.Point middle = span.start().add(span.end()).scale(0.5D);
                lightSample.set(floor(middle.x()), floor(middle.y()), floor(middle.z()));
                LightData light = lightSample.getLightData();
                emitter.emit(span, modelMin, modelMax, modelAnchor, tileModel,
                        light.getSkyLight(), light.getBlockLight());
            }
        } catch (MaxCapacityReachedException exception) {
            reset(tileModel, passStart);
        } catch (RuntimeException exception) {
            reset(tileModel, passStart);
            runtime.fail("wire-render-pass-failed");
        }
    }

    private static WireEmitter createEmitter(
            ResourcePack resourcePack,
            TextureGallery textureGallery
    ) {
        if (resourcePack.getTextures().get(WireEmitter.WIRE_TEXTURE) == null) {
            return null;
        }
        try {
            return new WireEmitter(textureGallery);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static void reset(TileModelView tileModel, int passStart) {
        tileModel.getTileModel().reset(passStart);
        tileModel.initialize(passStart);
    }
}
