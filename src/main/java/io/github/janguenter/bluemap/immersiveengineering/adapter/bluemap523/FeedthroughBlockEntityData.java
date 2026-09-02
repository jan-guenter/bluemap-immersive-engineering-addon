/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** BlueNBT projection of persisted feedthrough wire, middle block, and segment offset. */
public final class FeedthroughBlockEntityData extends MCABlockEntity {

    @NBTName("wire")
    private String wire;
    @NBTName("middle")
    private BlockState middle;
    @NBTName("offset")
    private int offset;

    public FeedthroughBlockEntityData() {
    }

    SpecialShapePlan.FeedthroughState state() {
        return wire == null || middle == null ? null : new SpecialShapePlan.FeedthroughState(
                wire, middle.getId().getFormatted(), offset
        );
    }
}
