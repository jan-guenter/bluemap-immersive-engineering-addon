/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.util.Key;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Strict Metal Press resource bundle compiled from the admitted IE JAR. */
public record MetalPressInstalledModel(
        WavefrontModel model,
        Map<String, Key> materials,
        Set<PartPosition> parts
) {

    private static final String ROOT =
            "assets/immersiveengineering/models/block/metal_multiblock/";
    private static final String NORMAL = ROOT + "metal_press_split.json";
    private static final String MIRRORED = ROOT + "metal_press_mirrored_split.json";
    private static final String OBJ = ROOT + "metal_press.obj";
    private static final String MTL = ROOT + "metal_press.mtl";
    private static final String MODEL_LOCATION =
            "immersiveengineering:models/block/metal_multiblock/metal_press.obj";
    private static final Set<PartPosition> EXPECTED_PARTS = Set.of(
            new PartPosition(0, -1, 0), new PartPosition(0, 0, 0),
            new PartPosition(0, 1, 0), new PartPosition(-1, -1, 0),
            new PartPosition(1, -1, 0), new PartPosition(-1, 0, 0),
            new PartPosition(1, 0, 0)
    );

    public MetalPressInstalledModel {
        materials = Map.copyOf(materials);
        parts = Set.copyOf(parts);
    }

    public static MetalPressInstalledModel load(Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Split normal = split(read(zip, NORMAL), false);
            Split mirrored = split(read(zip, MIRRORED), true);
            if (!normal.parts().equals(EXPECTED_PARTS)
                    || !mirrored.parts().equals(EXPECTED_PARTS)) {
                throw new IOException("Metal Press split roster changed");
            }
            WavefrontModel wavefront = WavefrontParser.parse(read(zip, OBJ));
            Map<String, String> rawMaterials = WavefrontParser.parseMaterials(read(zip, MTL));
            Map<String, Key> materials = new LinkedHashMap<>();
            rawMaterials.forEach((name, texture) -> materials.put(name, Key.parse(texture)));
            Set<String> referenced = new LinkedHashSet<>();
            wavefront.triangles().forEach(triangle -> referenced.add(triangle.material()));
            if (!materials.keySet().equals(referenced)) {
                throw new IOException("Metal Press OBJ/MTL material roster changed");
            }
            return new MetalPressInstalledModel(wavefront, materials, normal.parts());
        } catch (RuntimeException exception) {
            throw new IOException("invalid installed Metal Press resources", exception);
        }
    }

    static Split split(byte[] raw, boolean mirrored) throws IOException {
        JsonObject root = object(JsonParser.parseString(
                new String(raw, StandardCharsets.UTF_8)), "split root");
        boolean dynamic = root.has("dynamic") && booleanValue(root, "dynamic");
        if (!"immersiveengineering:basic_split".equals(string(root, "loader"))
                || dynamic) {
            throw new IOException("unexpected Metal Press split loader");
        }
        JsonObject inner = object(root.get("inner_model"), "inner model");
        if (mirrored) {
            if (!"immersiveengineering:mirror".equals(string(inner, "loader"))) {
                throw new IOException("missing Metal Press mirror loader");
            }
            inner = object(inner.get("inner_model"), "mirrored inner model");
        }
        if (!"neoforge:obj".equals(string(inner, "loader"))
                || !MODEL_LOCATION.equals(string(inner, "model"))
                || !booleanValue(inner, "flip_v")
                || booleanValue(inner, "automatic_culling")) {
            throw new IOException("unexpected Metal Press OBJ contract");
        }
        JsonArray splitParts = array(root.get("split_parts"), "split parts");
        Set<PartPosition> positions = new LinkedHashSet<>();
        for (JsonElement element : splitParts) {
            JsonArray tuple = array(element, "split position");
            if (tuple.size() != 3 || !positions.add(new PartPosition(
                    tuple.get(0).getAsInt(), tuple.get(1).getAsInt(),
                    tuple.get(2).getAsInt()))) {
                throw new IOException("invalid Metal Press split position");
            }
        }
        return new Split(Set.copyOf(positions), mirrored);
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory() || entry.getSize() > 2_000_000) {
            throw new IOException("missing or oversized installed resource " + path);
        }
        try (InputStream input = zip.getInputStream(entry)) {
            return input.readNBytes(2_000_001);
        }
    }

    private static JsonObject object(JsonElement value, String name) throws IOException {
        if (value == null || !value.isJsonObject()) {
            throw new IOException(name + " is not an object");
        }
        return value.getAsJsonObject();
    }

    private static JsonArray array(JsonElement value, String name) throws IOException {
        if (value == null || !value.isJsonArray()) {
            throw new IOException(name + " is not an array");
        }
        return value.getAsJsonArray();
    }

    private static String string(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString()) {
            throw new IOException(name + " is not a string");
        }
        return value.getAsString();
    }

    private static boolean booleanValue(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IOException(name + " is not a boolean");
        }
        return value.getAsBoolean();
    }

    /** Position relative to the master block in the north-facing model. */
    public record PartPosition(int x, int y, int z) {
    }

    record Split(Set<PartPosition> parts, boolean mirrored) {
    }
}
