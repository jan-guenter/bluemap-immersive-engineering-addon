/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.immersiveengineering.model.MetalPressInstalledModel.PartPosition;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetalPressBlockEntityDataTest {

    @Test
    void decodesExactCompoundPositionAndKeepsMasterDistinct() throws IOException {
        MetalPressDummyBlockEntityData dummy = readDummy(0, 1, 0);
        MetalPressMasterBlockEntityData master = readMaster();

        assertEquals(new PartPosition(0, 1, 0), dummy.position());
        assertEquals(MetalPressMasterBlockEntityData.class, master.getClass());
    }

    private static MetalPressDummyBlockEntityData readDummy(int x, int y, int z)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            common(writer, "immersiveengineering:metal_press_dummy");
            writer.name("posInMB").beginCompound();
            writer.name("X").value(x);
            writer.name("Y").value(y);
            writer.name("Z").value(z);
            writer.endCompound();
            writer.endCompound();
        }
        return read(bytes, MetalPressDummyBlockEntityData.class);
    }

    private static MetalPressMasterBlockEntityData readMaster() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            common(writer, "immersiveengineering:metal_press_master");
            writer.endCompound();
        }
        return read(bytes, MetalPressMasterBlockEntityData.class);
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
