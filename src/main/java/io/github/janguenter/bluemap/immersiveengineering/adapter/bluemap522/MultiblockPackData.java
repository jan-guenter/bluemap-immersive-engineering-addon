/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.immersiveengineering.model.InstalledMultiblockModel;

import java.util.Map;
import java.util.WeakHashMap;

/** Per-resource-pack compiled formed multiblock models. */
final class MultiblockPackData {

    private static final Map<ResourcePack, Map<String, InstalledMultiblockModel>> PACKS =
            new WeakHashMap<>();

    private MultiblockPackData() {
    }

    static synchronized void install(
            ResourcePack pack,
            Map<String, InstalledMultiblockModel> models
    ) {
        PACKS.put(pack, Map.copyOf(models));
    }

    static synchronized Map<String, InstalledMultiblockModel> get(ResourcePack pack) {
        return PACKS.get(pack);
    }
}
