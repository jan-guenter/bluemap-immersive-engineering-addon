/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel;

import java.util.Map;
import java.util.WeakHashMap;

/** Per-resource-pack compiled installed Metal Press data. */
final class MetalPressPackData {

    private static final Map<ResourcePack, MetalPressInstalledModel> PACKS = new WeakHashMap<>();

    private MetalPressPackData() {
    }

    static synchronized void install(ResourcePack pack, MetalPressInstalledModel model) {
        PACKS.put(pack, model);
    }

    static synchronized MetalPressInstalledModel get(ResourcePack pack) {
        return PACKS.get(pack);
    }
}
