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
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522.SpecialShapePlan.Box;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Emits axis-aligned special-shape boxes without importing client model classes. */
final class SpecialShapeEmitter {

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;

    SpecialShapeEmitter(ResourcePack resourcePack, TextureGallery textures, RenderSettings settings) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean emit(List<Box> boxes, BlockNeighborhood block, TileModelView target, Color mapColor) {
        Map<Key, Material> materials = resolve(boxes);
        if (boxes.isEmpty() || materials.size() != boxes.stream()
                .map(Box::texture).distinct().count()) {
            return false;
        }
        int start = target.getTileModel().size();
        for (Box box : boxes) {
            Material material = materials.get(box.texture());
            for (Direction face : Direction.values()) {
                if (settings.isRenderTopOnly() && face != Direction.UP) {
                    continue;
                }
                emitFace(box, face, material.index(), light(block, face), target);
            }
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

    private Map<Key, Material> resolve(List<Box> boxes) {
        Map<Key, Material> result = new LinkedHashMap<>();
        boxes.stream().map(Box::texture).distinct().forEach(key -> {
            Texture texture = resourcePack.getTextures().get(key);
            if (texture != null) {
                result.put(key, new Material(textures.get(key), texture));
            }
        });
        return result;
    }

    private Light light(BlockNeighborhood block, Direction face) {
        var vector = face.toVector();
        LightData own = block.getLightData();
        LightData neighbor = block.getNeighborBlock(
                vector.getX(), vector.getY(), vector.getZ()
        ).getLightData();
        int sunlight = Math.max(own.getSkyLight(), neighbor.getSkyLight());
        int blocklight = Math.max(own.getBlockLight(), neighbor.getBlockLight());
        int visible = settings.isCaveDetectionUsesBlockLight()
                ? Math.max(sunlight, blocklight) : sunlight;
        return new Light(sunlight, blocklight, !block.isRemoveIfCave() || visible != 0);
    }

    private static void emitFace(
            Box box,
            Direction face,
            int material,
            Light light,
            TileModelView target
    ) {
        if (!light.visible()) {
            return;
        }
        float[][] quad = vertices(box, face);
        triangle(quad[0], quad[1], quad[2], material, light, target);
        triangle(quad[0], quad[2], quad[3], material, light, target);
    }

    private static float[][] vertices(Box box, Direction face) {
        float x0 = box.minX();
        float y0 = box.minY();
        float z0 = box.minZ();
        float x1 = box.maxX();
        float y1 = box.maxY();
        float z1 = box.maxZ();
        return switch (face) {
            case UP -> points(x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            case DOWN -> points(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
            case NORTH -> points(x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
            case SOUTH -> points(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
            case WEST -> points(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
            case EAST -> points(x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        };
    }

    private static float[][] points(float... coordinates) {
        return new float[][]{
                {coordinates[0], coordinates[1], coordinates[2]},
                {coordinates[3], coordinates[4], coordinates[5]},
                {coordinates[6], coordinates[7], coordinates[8]},
                {coordinates[9], coordinates[10], coordinates[11]}
        };
    }

    private static void triangle(
            float[] first,
            float[] second,
            float[] third,
            int material,
            Light light,
            TileModelView target
    ) {
        int index = target.add(1);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(index,
                first[0], first[1], first[2],
                second[0], second[1], second[2],
                third[0], third[1], third[2]);
        mesh.setUvs(index, 0F, 0F, 0F, 1F, 1F, 1F);
        mesh.setMaterialIndex(index, material);
        mesh.setColor(index, 1F, 1F, 1F);
        mesh.setAOs(index, 1F, 1F, 1F);
        mesh.setSunlight(index, light.sunlight());
        mesh.setBlocklight(index, light.blocklight());
    }

    private record Material(int index, Texture texture) {
    }

    private record Light(int sunlight, int blocklight, boolean visible) {
    }
}
