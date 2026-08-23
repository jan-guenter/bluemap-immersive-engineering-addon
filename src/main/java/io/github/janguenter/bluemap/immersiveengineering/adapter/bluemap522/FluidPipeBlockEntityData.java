/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.EnumSet;
import java.util.Set;

/** BlueNBT projection of IE's persisted six-bit fluid-pipe connection mask. */
public final class FluidPipeBlockEntityData extends MCABlockEntity {

    @NBTName("connections")
    private byte connections;
    @NBTName("sideConfig")
    private byte[] sideConfig;

    public FluidPipeBlockEntityData() {
    }

    Set<SpecialShapePlan.Face> connectedFaces() {
        int mask = Byte.toUnsignedInt(connections);
        EnumSet<SpecialShapePlan.Face> result = EnumSet.noneOf(SpecialShapePlan.Face.class);
        SpecialShapePlan.Face[] order = SpecialShapePlan.Face.values();
        for (int bit = 0; bit < order.length; bit++) {
            if ((mask & 1 << bit) != 0) {
                result.add(order[bit]);
            }
        }
        return Set.copyOf(result);
    }

    byte[] sideConfig() {
        return sideConfig == null ? new byte[0] : sideConfig.clone();
    }
}
