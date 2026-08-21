/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;

/** Exact BlueNBT projection of the dummy part's persisted template position. */
public final class MetalPressDummyBlockEntityData extends MCABlockEntity {

    @NBTName("posInMB")
    private Position position;

    public MetalPressDummyBlockEntityData() {
    }

    PartPosition position() {
        return position == null ? null : new PartPosition(position.x, position.y, position.z);
    }

    /** Minecraft's compound BlockPos codec uses uppercase coordinate keys. */
    public static final class Position {
        @NBTName("X")
        private int x;
        @NBTName("Y")
        private int y;
        @NBTName("Z")
        private int z;

        public Position() {
        }
    }
}
