/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** BlueNBT projection shared by the six exact conveyor block-entity IDs. */
public final class ConveyorBlockEntityData extends MCABlockEntity {

    @NBTName("conveyorBeltSubtypeNBT")
    private Subtype subtype;

    public ConveyorBlockEntityData() {
    }

    SpecialShapePlan.ConveyorState state() {
        return subtype == null ? null : new SpecialShapePlan.ConveyorState(
                subtype.direction,
                subtype.cover,
                subtype.relativeExtractDir,
                subtype.panelRight != 0,
                subtype.nextLeft != 0
        );
    }

    /** Persisted subtype fields used by the special-page fixtures. */
    public static final class Subtype {

        @NBTName("direction")
        private int direction;
        @NBTName("cover")
        private String cover;
        @NBTName("relativeExtractDir")
        private int relativeExtractDir;
        @NBTName("panelRight")
        private byte panelRight;
        @NBTName("nextLeft")
        private byte nextLeft;

        public Subtype() {
        }
    }
}
