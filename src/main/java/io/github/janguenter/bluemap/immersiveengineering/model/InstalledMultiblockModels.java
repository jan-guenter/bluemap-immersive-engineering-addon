/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModel.PartPosition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Compiles every static formed-multiblock OBJ used by the gallery. */
public final class InstalledMultiblockModels {

    private static final String MODEL_ROOT =
            "assets/immersiveengineering/models/";
    private static final List<Spec> SPECS = List.of(
            new Spec("coke_oven", "block/coke_oven_off_split"),
            new Spec("blast_furnace", "block/blast_furnace_off_split"),
            new Spec("advanced_blast_furnace", "block/blastfurnace_advanced_split"),
            new Spec("alloy_smelter", "block/alloy_smelter_off_split"),
            metal("crusher"),
            metal("fermenter"),
            metal("diesel_generator"),
            metal("metal_press"),
            metal("assembler"),
            metal("auto_workbench"),
            metal("bottling_machine"),
            metal("mixer"),
            metal("refinery"),
            metal("squeezer"),
            metal("sawmill"),
            metal("arc_furnace"),
            metal("silo"),
            metal("shelf"),
            metal("tank"),
            metal("chunk_loader"),
            new Spec("lightning_rod", "block/metal_multiblock/lightningrod_split"),
            metal("excavator"),
            metal("radio_tower")
    );

    private InstalledMultiblockModels() {
    }

    public static Map<String, InstalledMultiblockModel> load(Path jar) throws IOException {
        Map<String, InstalledMultiblockModel> result = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (Spec spec : SPECS) {
                InstalledMultiblockModel model;
                try {
                    model = load(zip, spec);
                } catch (IOException exception) {
                    throw new IOException(
                            "invalid installed model for " + spec.block(), exception
                    );
                }
                if (result.put(model.blockId(), model) != null) {
                    throw new IOException("duplicate formed multiblock model");
                }
            }
            InstalledMultiblockModel bucketWheel = loadBucketWheel(zip);
            if (result.put(bucketWheel.blockId(), bucketWheel) != null) {
                throw new IOException("duplicate Bucket Wheel model");
            }
        } catch (RuntimeException exception) {
            throw new IOException("invalid installed formed multiblock resources", exception);
        }
        return Map.copyOf(result);
    }

    private static InstalledMultiblockModel loadBucketWheel(ZipFile zip) throws IOException {
        String root = "assets/immersiveengineering/models/block/metal_multiblock/";
        WavefrontModel wavefront = WavefrontParser.parse(
                read(zip, root + "bucket_wheel.obj.ie")
        );
        Map<String, String> rawMaterials = WavefrontParser.parseMaterials(
                read(zip, root + "bucket_wheel.mtl")
        );
        Set<String> referenced = new LinkedHashSet<>();
        wavefront.triangles().forEach(triangle -> referenced.add(triangle.material()));
        if (!rawMaterials.keySet().containsAll(referenced)) {
            throw new IOException("Bucket Wheel OBJ material roster changed");
        }
        Map<String, Key> materials = new LinkedHashMap<>();
        referenced.forEach(name -> materials.put(name, Key.parse(rawMaterials.get(name))));
        return new InstalledMultiblockModel(
                "immersiveengineering:bucket_wheel", wavefront, materials,
                Set.of(new PartPosition(0, 0, 0))
        );
    }

    private static InstalledMultiblockModel load(ZipFile zip, Spec spec) throws IOException {
        JsonObject split = object(JsonParser.parseString(new String(
                read(zip, MODEL_ROOT + spec.model() + ".json"), StandardCharsets.UTF_8
        )), "split root");
        if (!"immersiveengineering:basic_split".equals(string(split, "loader"))) {
            throw new IOException("unexpected split loader for " + spec.block());
        }
        JsonObject inner = object(split.get("inner_model"), "inner model");
        String loader = string(inner, "loader");
        if (!Set.of("neoforge:obj", "immersiveengineering:ie_obj").contains(loader)) {
            throw new IOException("unexpected OBJ loader for " + spec.block());
        }
        String location = string(inner, "model");
        String objectPath = resourcePath(location);
        WavefrontModel wavefront = WavefrontParser.parse(read(zip, objectPath));
        String materialPath = objectPath.replaceFirst("\\.obj(?:\\.ie)?$", ".mtl");
        Map<String, String> aliases = textureAliases(inner);
        Map<String, String> rawMaterials = WavefrontParser.parseMaterials(
                read(zip, materialPath), aliases
        );
        Set<String> referenced = new LinkedHashSet<>();
        wavefront.triangles().forEach(triangle -> referenced.add(triangle.material()));
        if (!rawMaterials.keySet().containsAll(referenced)) {
            throw new IOException("OBJ material roster changed for " + spec.block());
        }
        Map<String, Key> materials = new LinkedHashMap<>();
        referenced.forEach(name -> materials.put(name, Key.parse(rawMaterials.get(name))));
        Set<PartPosition> parts = parts(split);
        return new InstalledMultiblockModel(
                "immersiveengineering:" + spec.block(), wavefront, materials, parts
        );
    }

    private static Set<PartPosition> parts(JsonObject split) throws IOException {
        JsonArray values = array(split.get("split_parts"), "split parts");
        Set<PartPosition> result = new LinkedHashSet<>();
        for (JsonElement value : values) {
            JsonArray tuple = array(value, "split position");
            if (tuple.size() != 3 || !result.add(new PartPosition(
                    tuple.get(0).getAsInt(), tuple.get(1).getAsInt(),
                    tuple.get(2).getAsInt()))) {
                throw new IOException("invalid split position");
            }
        }
        if (!result.contains(new PartPosition(0, 0, 0))) {
            throw new IOException("split model lacks master origin");
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
            if (!entry.getValue().isJsonPrimitive()
                    || !entry.getValue().getAsJsonPrimitive().isString()) {
                throw new IOException("invalid OBJ texture alias");
            }
            result.put(entry.getKey(), entry.getValue().getAsString());
        }
        return Map.copyOf(result);
    }

    private static String resourcePath(String location) throws IOException {
        String[] parts = location.split(":", 2);
        if (parts.length != 2 || !parts[0].matches("[a-z0-9_.-]+")
                || !parts[1].matches("[a-z0-9_./-]+\\.obj(?:\\.ie)?")) {
            throw new IOException("invalid installed OBJ location");
        }
        return "assets/" + parts[0] + "/" + parts[1];
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

    private static Spec metal(String block) {
        return new Spec(block, "block/metal_multiblock/" + block + "_split");
    }

    private record Spec(String block, String model) {
    }
}
