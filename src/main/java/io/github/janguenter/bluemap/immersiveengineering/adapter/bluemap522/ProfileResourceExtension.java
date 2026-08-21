/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.immersiveengineering.activation.AddonRuntime;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel;
import io.github.janguenter.bluemap.immersiveengineering.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.immersiveengineering.profile.ImmersiveEngineering1242194Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Exact-artifact admission and installed Metal Press resource compilation. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final Key SYNTHETIC =
            Key.parse("bluemap_immersive_engineering:metal_press");
    private static final String METAL_PRESS = "immersiveengineering:metal_press";

    private final ResourcePack resourcePack;
    private final AddonRuntime runtime;
    private MetalPressInstalledModel installed;
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
            installed = MetalPressInstalledModel.load(artifacts.get(0));
            var dispatch = resourcePack.getBlockStates().get(SYNTHETIC);
            if (!validDispatch(dispatch)) {
                throw new IOException("synthetic Metal Press dispatch invalid");
            }
        } catch (IOException | RuntimeException exception) {
            installed = null;
            runtime.inactive("metal-press-resource-" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return installed == null ? Set.of() : Set.copyOf(installed.materials().values());
    }

    @Override
    public void bake() {
        if (installed == null
                || resourcePack.getBlockStates().get(Key.parse(METAL_PRESS)) == null
                || installed.materials().values().stream().anyMatch(key ->
                resourcePack.getTextures().get(key) == null)) {
            runtime.inactive("metal-press-bake-incomplete");
            return;
        }
        MetalPressPackData.install(resourcePack, installed);
        ready = true;
        runtime.activate();
        System.out.println("BlueMap Immersive Engineering add-on active: Metal Press whole model.");
    }

    @Override
    public Key getBlockStateKey(Key key) {
        return ready && METAL_PRESS.equals(key.getFormatted()) ? SYNTHETIC : key;
    }

    @Override
    public void getBlockProperties(BlockState state, BlockProperties.Builder builder) {
        if (ready && METAL_PRESS.equals(state.getId().getFormatted())) {
            builder.culling(false).occluding(false).cullingIdentical(false);
        }
    }

    private static boolean validDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        if (state == null || state.getMultipart() != null) {
            return false;
        }
        Variants variants = state.getVariants();
        if (variants == null || variants.getDefaultVariant() == null
                || variants.getDefaultVariant().getVariants().length != 1) {
            return false;
        }
        Variant variant = variants.getDefaultVariant().getVariants()[0];
        return BlueMap522Adapter.isExpectedDispatch(variant);
    }
}
