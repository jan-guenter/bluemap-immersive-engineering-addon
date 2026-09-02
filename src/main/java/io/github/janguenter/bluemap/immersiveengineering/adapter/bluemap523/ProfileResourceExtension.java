/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.SyntheticDispatch;
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModel;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModels;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledSpecialModels;
import io.github.janguenter.bluemap.immersiveengineering.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.immersiveengineering.profile.ImmersiveEngineering1242194Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact-artifact admission and installed formed-multiblock resource compilation. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final Key SYNTHETIC =
            Key.parse("bluemap_immersive_engineering:formed_multiblock");
    private static final Key SPECIAL_SYNTHETIC =
            SpecialShapeIntegration.SYNTHETIC_BLOCK_STATE;

    private final ResourcePack resourcePack;
    private final AddonRuntime runtime;
    private Map<String, InstalledMultiblockModel> installed;
    private boolean ready;

    ProfileResourceExtension(ResourcePack resourcePack, AddonRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.immersiveengineering.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        List<Path> artifacts = ExactArtifactDetector.findAll(
                roots, ImmersiveEngineering1242194Profile.ARTIFACTS
        ).orElse(null);
        if (artifacts == null) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        try {
            Map<String, InstalledMultiblockModel> models = new LinkedHashMap<>(
                    InstalledMultiblockModels.load(artifacts.get(0))
            );
            for (Map.Entry<String, InstalledMultiblockModel> special
                    : InstalledSpecialModels.load(artifacts.get(0)).entrySet()) {
                if (models.putIfAbsent(special.getKey(), special.getValue()) != null) {
                    throw new IOException("duplicate installed model " + special.getKey());
                }
            }
            installed = Map.copyOf(models);
            SpecialShapeResources.verifyExactJar(artifacts.get(0));
            var dispatch = resourcePack.getBlockStates().get(SYNTHETIC);
            if (!validDispatch(dispatch)) {
                throw new IOException("synthetic formed multiblock dispatch invalid");
            }
            var specialDispatch = resourcePack.getBlockStates().get(SPECIAL_SYNTHETIC);
            if (!validSpecialDispatch(specialDispatch)) {
                throw new IOException("synthetic special-shape dispatch invalid");
            }
        } catch (IOException | RuntimeException exception) {
            installed = null;
            runtime.inactive("multiblock-resource-" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        if (installed == null) {
            return Set.of();
        }
        Set<Key> textures = new LinkedHashSet<>(
                SpecialShapeIntegration.requiredTextureKeys()
        );
        installed.values().forEach(model -> textures.addAll(model.materials().values()));
        textures.add(WireEmitter.WIRE_TEXTURE);
        return Set.copyOf(textures);
    }

    @Override
    public void bake() {
        if (installed == null
                || installed.keySet().stream().anyMatch(block ->
                resourcePack.getBlockStates().get(Key.parse(block)) == null)
                || installed.values().stream().flatMap(model ->
                model.materials().values().stream()).anyMatch(key ->
                resourcePack.getTextures().get(key) == null)
                || SpecialShapeIntegration.requiredTextureKeys().stream().anyMatch(key ->
                resourcePack.getTextures().get(key) == null)
                || resourcePack.getTextures().get(WireEmitter.WIRE_TEXTURE) == null) {
            runtime.inactive("multiblock-bake-incomplete");
            return;
        }
        MultiblockPackData.install(resourcePack, installed);
        ready = true;
        runtime.activate();
        System.out.println("BlueMap Immersive Engineering add-on active: "
                + installed.size() + " installed models.");
    }

    @Override
    public Key getBlockStateKey(Key key) {
        if (ready && SpecialShapeIntegration.covers(key)) {
            return SPECIAL_SYNTHETIC;
        }
        return ready && installed.containsKey(key.getFormatted()) ? SYNTHETIC : key;
    }

    @Override
    public void getBlockProperties(BlockState state, BlockProperties.Builder builder) {
        if (ready && (installed.containsKey(state.getId().getFormatted())
                || SpecialShapeIntegration.covers(state.getId()))) {
            builder.culling(false).occluding(false).cullingIdentical(false);
        }
    }

    private static boolean validDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        return SyntheticDispatch.matches(state, BlueMap523Adapter.RENDERER);
    }

    private static boolean validSpecialDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        return SyntheticDispatch.matches(state, BlueMap523Adapter.SPECIAL_RENDERER);
    }
}
