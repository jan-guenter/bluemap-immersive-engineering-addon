/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;

/** Exact BlueNBT projection of the dummy part's persisted template position. */
public final class MetalPressDummyBlockEntityData extends MCABlockEntity {

    @NBTName("posInMB")
    private int[] position;

    public MetalPressDummyBlockEntityData() {
    }

    PartPosition position() {
        return position == null || position.length != 3
                ? null : new PartPosition(position[0], position[1], position[2]);
    }
}
