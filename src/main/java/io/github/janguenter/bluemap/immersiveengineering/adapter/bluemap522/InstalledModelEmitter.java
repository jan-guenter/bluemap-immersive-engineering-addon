/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModel;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledModelTransform;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Vertex;

import java.util.LinkedHashMap;
import java.util.Map;

/** Emits one static installed whole model from its formed master block. */
final class InstalledModelEmitter {

    private static final String COKE_OVEN = "immersiveengineering:coke_oven";

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;

    InstalledModelEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean emit(
            InstalledMultiblockModel installed,
            boolean mirrored,
            String facing,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Map<String, Material> materials = resolve(installed.materials());
        if (materials.size() != installed.materials().size()) {
            return false;
        }
        int start = target.getTileModel().size();
        for (Triangle source : installed.model().triangles()) {
            Triangle triangle = InstalledModelTransform.triangle(
                    source, installed.blockId(), mirrored, facing
            );
            Direction direction = nearestDirection(triangle);
            if (settings.isRenderTopOnly() && direction != Direction.UP) {
                continue;
            }
            var normal = direction.toVector();
            int lightDistance = COKE_OVEN.equals(installed.blockId()) ? 2 : 1;
            LightData own = block.getLightData();
            LightData faced = block.getNeighborBlock(
                    normal.getX() * lightDistance,
                    normal.getY() * lightDistance,
                    normal.getZ() * lightDistance
            ).getLightData();
            int sunlight = Math.max(own.getSkyLight(), faced.getSkyLight());
            int blocklight = Math.max(own.getBlockLight(), faced.getBlockLight());
            int visible = settings.isCaveDetectionUsesBlockLight()
                    ? Math.max(sunlight, blocklight) : sunlight;
            if (block.isRemoveIfCave() && visible == 0) {
                continue;
            }
            Material material = materials.get(triangle.material());
            if (material == null) {
                return false;
            }
            int index = target.add(1);
            TileModel mesh = target.getTileModel();
            positions(mesh, index, triangle);
            uvs(mesh, index, triangle);
            mesh.setMaterialIndex(index, material.index());
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, sunlight);
            mesh.setBlocklight(index, blocklight);
        }
        if (target.getTileModel().size() == start) {
            return false;
        }
        target.initialize(start);
        materials.values().stream().map(Material::texture).distinct().forEach(texture ->
                mapColor.add(new Color().set(texture.getColorPremultiplied()))
        );
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return true;
    }

    private Map<String, Material> resolve(Map<String, Key> mappings) {
        Map<String, Material> result = new LinkedHashMap<>();
        mappings.forEach((name, path) -> {
            Texture texture = resourcePack.getTextures().get(path);
            if (texture != null) {
                result.put(name, new Material(textures.get(path), texture));
            }
        });
        return result;
    }

    private static Direction nearestDirection(Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        float abx = b.x() - a.x();
        float aby = b.y() - a.y();
        float abz = b.z() - a.z();
        float acx = c.x() - a.x();
        float acy = c.y() - a.y();
        float acz = c.z() - a.z();
        float x = aby * acz - abz * acy;
        float y = abz * acx - abx * acz;
        float z = abx * acy - aby * acx;
        float ax = Math.abs(x);
        float ay = Math.abs(y);
        float az = Math.abs(z);
        if (ay >= ax && ay >= az) {
            return y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (ax >= az) {
            return x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void positions(TileModel mesh, int index, Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        mesh.setPositions(index, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
    }

    private static void uvs(TileModel mesh, int index, Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        mesh.setUvs(index, a.u(), a.v(), b.u(), b.v(), c.u(), c.v());
    }

    private record Material(int index, Texture texture) {
    }
}
