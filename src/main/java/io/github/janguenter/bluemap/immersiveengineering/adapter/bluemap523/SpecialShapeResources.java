/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipFile;

/** Exact runtime resource contract used by the bounded special-shape renderer. */
public final class SpecialShapeResources {

    static final Key CONVEYOR = key("immersiveengineering:block/conveyor/conveyor");
    static final Key CASING = key("immersiveengineering:block/conveyor/casing_side");
    static final Key DROPPER = key("immersiveengineering:block/conveyor/dropper_off");
    static final Key REDSTONE = key("immersiveengineering:block/conveyor/redstone");
    static final Key SPLITTER = key("immersiveengineering:block/conveyor/split_off");
    static final Key VERTICAL = key("immersiveengineering:block/conveyor/vertical_off");
    static final Key CONNECTOR_LV = key("immersiveengineering:block/connector/connector_lv");
    static final Key CONNECTOR_MV = key("immersiveengineering:block/connector/connector_mv");
    static final Key CONNECTOR_HV = key("immersiveengineering:block/connector/connector_hv");
    static final Key CONNECTOR_REDSTONE =
            key("immersiveengineering:block/connector/connector_redstone");
    static final Key FLUID_PIPE = key("immersiveengineering:block/metal_device/fluid_pipe");

    private static final Set<Key> REQUIRED_TEXTURES = Set.of(
            CONVEYOR, CASING, DROPPER, REDSTONE, SPLITTER, VERTICAL,
            CONNECTOR_LV, CONNECTOR_MV, CONNECTOR_HV, CONNECTOR_REDSTONE, FLUID_PIPE,
            key("minecraft:block/bricks"), key("minecraft:block/glass"),
            key("minecraft:block/copper_block"), key("minecraft:block/oak_planks")
    );

    private SpecialShapeResources() {
    }

    /** Texture keys the profile extension must include in its bake admission check. */
    public static Set<Key> requiredTextureKeys() {
        return REQUIRED_TEXTURES;
    }

    /**
     * Verifies that every IE-owned texture comes from the admitted IE JAR.
     * Vanilla textures are deliberately excluded because they come from the Minecraft pack.
     */
    public static void verifyExactJar(Path immersiveEngineeringJar) throws IOException {
        Set<String> missing = new LinkedHashSet<>();
        try (ZipFile zip = new ZipFile(immersiveEngineeringJar.toFile())) {
            for (Key texture : REQUIRED_TEXTURES) {
                if (!"immersiveengineering".equals(texture.getNamespace())) {
                    continue;
                }
                String entry = "assets/" + texture.getNamespace() + "/textures/"
                        + texture.getValue() + ".png";
                if (zip.getEntry(entry) == null) {
                    missing.add(entry);
                }
            }
        }
        if (!missing.isEmpty()) {
            throw new IOException("IE special renderer resources missing: " + missing);
        }
    }

    static Key middleTexture(String blockId) {
        return switch (blockId) {
            case "minecraft:bricks" -> key("minecraft:block/bricks");
            case "minecraft:glass" -> key("minecraft:block/glass");
            case "minecraft:copper_block" -> key("minecraft:block/copper_block");
            case "minecraft:oak_planks" -> key("minecraft:block/oak_planks");
            default -> null;
        };
    }

    static Key connectorTexture(String wire) {
        return switch (wire) {
            case "COPPER" -> CONNECTOR_LV;
            case "ELECTRUM" -> CONNECTOR_MV;
            case "STEEL" -> CONNECTOR_HV;
            case "REDSTONE" -> CONNECTOR_REDSTONE;
            default -> null;
        };
    }

    private static Key key(String value) {
        return Key.parse(value);
    }
}
