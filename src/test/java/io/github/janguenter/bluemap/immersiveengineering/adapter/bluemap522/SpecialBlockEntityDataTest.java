/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522.SpecialShapePlan.Face;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialBlockEntityDataTest {

    @Test
    void decodesPersistedConveyorSubtypeCompound() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            common(writer, "immersiveengineering:redstone");
            writer.name("conveyorBeltSubtypeNBT").beginCompound();
            writer.name("direction").value(2);
            writer.name("panelRight").value((byte) 1);
            writer.endCompound();
            writer.endCompound();
        }
        var state = read(bytes, ConveyorBlockEntityData.class).state();
        assertEquals(2, state.direction());
        assertTrue(state.panelRight());
    }

    @Test
    void decodesFeedthroughAndFluidPipeFields() throws IOException {
        ByteArrayOutputStream feedthrough = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(feedthrough)) {
            writer.beginCompound();
            common(writer, "immersiveengineering:feedthrough");
            writer.name("wire").value("COPPER");
            writer.name("middle").beginCompound();
            writer.name("Name").value("minecraft:bricks");
            writer.endCompound();
            writer.name("offset").value(-1);
            writer.endCompound();
        }
        var feedthroughState = read(feedthrough, FeedthroughBlockEntityData.class).state();
        assertEquals("COPPER", feedthroughState.wire());
        assertEquals("minecraft:bricks", feedthroughState.middleBlockId());
        assertEquals(-1, feedthroughState.offset());

        ByteArrayOutputStream pipe = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(pipe)) {
            writer.beginCompound();
            common(writer, "immersiveengineering:fluidpipe");
            writer.name("connections").value((byte) 0b110010);
            writer.name("sideConfig").value(new byte[]{0, 1, 2, 0, 0, 0});
            writer.endCompound();
        }
        FluidPipeBlockEntityData pipeData = read(pipe, FluidPipeBlockEntityData.class);
        assertEquals(Set.of(Face.UP, Face.WEST, Face.EAST), pipeData.connectedFaces());
        assertEquals(6, pipeData.sideConfig().length);
    }

    private static void common(NBTWriter writer, String id) throws IOException {
        writer.name("id").value(id);
        writer.name("x").value(10);
        writer.name("y").value(100);
        writer.name("z").value(10);
    }

    private static <T> T read(ByteArrayOutputStream bytes, Class<T> type) throws IOException {
        return MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(bytes.toByteArray()), type
        );
    }
}
