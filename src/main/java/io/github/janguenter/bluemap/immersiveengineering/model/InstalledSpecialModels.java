/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModel.PartPosition;
import io.github.janguenter.bluemap.immersiveengineering.model.WavefrontModel.Triangle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Compiles the static OBJ-backed blocks used by the special-renderer gallery. */
public final class InstalledSpecialModels {

    private static final String MODEL_ROOT = "assets/immersiveengineering/models/";
    private static final Set<PartPosition> SINGLE_PART = Set.of(
            new PartPosition(0, 0, 0)
    );
    private static final Set<PartPosition> TURRET_PARTS = Set.of(
            new PartPosition(0, 0, 0), new PartPosition(0, 1, 0)
    );
    private static final List<Spec> SPECS = List.of(
            direct("connector_lv", "block/connector/connector_lv"),
            direct("connector_lv_relay", "block/connector/relay_lv"),
            direct("connector_mv", "block/connector/connector_mv"),
            direct("connector_mv_relay", "block/connector/relay_mv"),
            direct("connector_hv", "block/connector/connector_hv"),
            direct("connector_hv_relay", "block/connector/relay_hv"),
            direct("connector_redstone", "block/connector/connector_redstone"),
            direct("connector_structural", "block/connector/connector_structural"),
            direct("charging_station", "metal_device/charging_station"),
            split("sample_drill", "block/metal_device/core_drill_split"),
            split("tesla_coil", "block/metal_device/teslacoil_split"),
            new Spec("turret_chem", "block/metal_device/chem_turret", TURRET_PARTS),
            new Spec("turret_gun", "block/metal_device/gun_turret", TURRET_PARTS),
            split("cloche", "block/metal_device/cloche_split"),
            split(
                    "blastfurnace_preheater",
                    "block/metal_device/blastfurnace_preheater_split"
            ),
            direct("watermill", "dynamic/watermill"),
            direct("windmill", "dynamic/windmill"),
            split("workbench", "block/wooden_device/workbench_split"),
            direct("coresample", "block/coresample")
    );
    private static final Set<String> BLOCK_IDS = blockIds(SPECS);

    private InstalledSpecialModels() {
    }

    /** Exact block IDs admitted by this installed-resource loader. */
    public static Set<String> blockIds() {
        return BLOCK_IDS;
    }

    /** Loads every special model from one admitted Immersive Engineering JAR. */
    public static Map<String, InstalledMultiblockModel> load(Path jar) throws IOException {
        Map<String, InstalledMultiblockModel> result = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (Spec spec : SPECS) {
                InstalledMultiblockModel model;
                try {
                    model = load(zip, spec);
                } catch (IOException exception) {
                    throw new IOException(
                            "invalid installed special model for " + spec.block(), exception
                    );
                }
                if (result.put(model.blockId(), model) != null) {
                    throw new IOException("duplicate installed special model");
                }
            }
        } catch (RuntimeException exception) {
            throw new IOException("invalid installed special-model resources", exception);
        }
        if (!result.keySet().equals(BLOCK_IDS)) {
            throw new IOException("installed special-model roster changed");
        }
        return Map.copyOf(result);
    }

    private static InstalledMultiblockModel load(ZipFile zip, Spec spec) throws IOException {
        JsonObject document = object(JsonParser.parseString(new String(
                read(zip, MODEL_ROOT + spec.model() + ".json"), StandardCharsets.UTF_8
        )), "special model root");
        Set<PartPosition> parts = spec.parts() == null ? parts(document) : spec.parts();
        List<JsonObject> components = components(document);
        List<Triangle> triangles = new ArrayList<>();
        Map<String, Key> materials = new LinkedHashMap<>();
        for (JsonObject component : components) {
            loadComponent(zip, component, triangles, materials);
        }
        if (triangles.isEmpty() || materials.isEmpty()) {
            throw new IOException("special model has no textured geometry");
        }
        return new InstalledMultiblockModel(
                "immersiveengineering:" + spec.block(),
                new WavefrontModel(triangles), materials, parts
        );
    }

    private static List<JsonObject> components(JsonObject document) throws IOException {
        String loader = string(document, "loader");
        if ("immersiveengineering:basic_split".equals(loader)) {
            return List.of(unwrap(object(document.get("inner_model"), "split inner model")));
        }
        if ("neoforge:composite".equals(loader)) {
            JsonObject children = object(document.get("children"), "composite children");
            if (children.size() == 0) {
                throw new IOException("composite model has no children");
            }
            List<JsonObject> result = new ArrayList<>();
            for (Map.Entry<String, JsonElement> child : children.entrySet()) {
                result.add(unwrap(object(child.getValue(), "composite child")));
            }
            return List.copyOf(result);
        }
        return List.of(unwrap(document));
    }

    private static JsonObject unwrap(JsonObject model) throws IOException {
        String loader = string(model, "loader");
        if ("immersiveengineering:mirror".equals(loader)) {
            return unwrap(object(model.get("inner_model"), "mirrored inner model"));
        }
        if (!Set.of("neoforge:obj", "immersiveengineering:ie_obj").contains(loader)) {
            throw new IOException("unexpected special-model loader " + loader);
        }
        return model;
    }

    private static void loadComponent(
            ZipFile zip,
            JsonObject component,
            List<Triangle> triangles,
            Map<String, Key> materials
    ) throws IOException {
        String objectPath = resourcePath(string(component, "model"));
        byte[] objectBytes = read(zip, objectPath);
        WavefrontModel wavefront = WavefrontParser.parse(objectBytes);
        String materialPath = materialPath(objectPath, objectBytes);
        Map<String, String> rawMaterials = WavefrontParser.parseMaterials(
                read(zip, materialPath), textureAliases(component)
        );
        Set<String> referenced = new LinkedHashSet<>();
        wavefront.triangles().forEach(triangle -> referenced.add(triangle.material()));
        if (!rawMaterials.keySet().containsAll(referenced)) {
            throw new IOException("special OBJ material roster changed");
        }
        for (String name : referenced) {
            Key texture = Key.parse(rawMaterials.get(name));
            Key previous = materials.putIfAbsent(name, texture);
            if (previous != null && !previous.equals(texture)) {
                throw new IOException("conflicting special OBJ material");
            }
        }
        triangles.addAll(wavefront.triangles());
    }

    private static Set<PartPosition> parts(JsonObject split) throws IOException {
        if (!"immersiveengineering:basic_split".equals(string(split, "loader"))) {
            throw new IOException("expected split special model");
        }
        JsonArray values = array(split.get("split_parts"), "split parts");
        Set<PartPosition> result = new LinkedHashSet<>();
        for (JsonElement value : values) {
            JsonArray tuple = array(value, "split position");
            if (tuple.size() != 3 || !result.add(new PartPosition(
                    integer(tuple.get(0)), integer(tuple.get(1)), integer(tuple.get(2))
            ))) {
                throw new IOException("invalid special split position");
            }
        }
        if (!result.contains(new PartPosition(0, 0, 0))) {
            throw new IOException("special split model lacks master origin");
        }
        return Set.copyOf(result);
    }

    private static Map<String, String> textureAliases(JsonObject model) throws IOException {
        JsonElement element = model.get("textures");
        if (element == null) {
            return Map.of();
        }
        JsonObject textures = object(element, "OBJ textures");
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new IOException("invalid special OBJ texture alias");
            }
            result.put(entry.getKey(), value.getAsString());
        }
        return Map.copyOf(result);
    }

    private static String resourcePath(String location) throws IOException {
        String[] parts = location.split(":", 2);
        if (parts.length != 2 || !parts[0].matches("[a-z0-9_.-]+")
                || !parts[1].matches("[a-z0-9_./-]+\\.obj(?:\\.ie)?")) {
            throw new IOException("invalid installed special OBJ location");
        }
        return "assets/" + parts[0] + '/' + parts[1];
    }

    private static String materialPath(String objectPath, byte[] objectBytes)
            throws IOException {
        String library = null;
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(
                new String(objectBytes, StandardCharsets.UTF_8)
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (++lines > 100_000) {
                    throw new IOException("special OBJ exceeds line bound");
                }
                line = line.trim();
                if (!line.startsWith("mtllib ")) {
                    continue;
                }
                String candidate = line.substring(7).trim();
                if (!candidate.matches("[a-z0-9_.-]+\\.mtl")
                        || library != null && !library.equals(candidate)) {
                    throw new IOException("invalid special OBJ material library");
                }
                library = candidate;
            }
        }
        if (library == null) {
            throw new IOException("special OBJ has no material library");
        }
        int slash = objectPath.lastIndexOf('/');
        return objectPath.substring(0, slash + 1) + library;
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory() || entry.getSize() > 2_000_000) {
            throw new IOException("missing or oversized installed resource " + path);
        }
        try (InputStream input = zip.getInputStream(entry)) {
            byte[] raw = input.readNBytes(2_000_001);
            if (raw.length > 2_000_000) {
                throw new IOException("oversized installed resource " + path);
            }
            return raw;
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

    private static int integer(JsonElement value) throws IOException {
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isNumber()) {
            throw new IOException("split coordinate is not an integer");
        }
        try {
            return value.getAsInt();
        } catch (NumberFormatException exception) {
            throw new IOException("invalid split coordinate", exception);
        }
    }

    private static Set<String> blockIds(List<Spec> specs) {
        Set<String> result = new LinkedHashSet<>();
        for (Spec spec : specs) {
            if (!result.add("immersiveengineering:" + spec.block())) {
                throw new IllegalStateException("duplicate special-model block ID");
            }
        }
        return Set.copyOf(result);
    }

    private static Spec direct(String block, String model) {
        return new Spec(block, model, SINGLE_PART);
    }

    private static Spec split(String block, String model) {
        return new Spec(block, model, null);
    }

    private record Spec(String block, String model, Set<PartPosition> parts) {
        private Spec {
            parts = parts == null ? null : Set.copyOf(parts);
        }
    }
}
