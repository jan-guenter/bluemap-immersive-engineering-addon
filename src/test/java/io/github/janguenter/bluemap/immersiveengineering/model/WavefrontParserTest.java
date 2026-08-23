/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WavefrontParserTest {

    @Test
    void triangulatesAndFlipsVWhileReadingDirectMaterials() throws IOException {
        WavefrontModel model = WavefrontParser.parse(("""
                v 0 0 0
                v 1 0 0
                v 1 1 0
                v 0 1 0
                vt 0 0
                vt 1 0
                vt 1 1
                vt 0 1
                usemtl press
                f 1/1 2/2 3/3 4/4
                """).getBytes(StandardCharsets.UTF_8));

        assertEquals(2, model.triangles().size());
        assertEquals(1F, model.triangles().get(0).first().v());
        assertEquals(0F, model.triangles().get(0).third().v());
        assertEquals(Map.of("press", "immersiveengineering:block/press"),
                WavefrontParser.parseMaterials(("""
                        newmtl press
                        map_Kd immersiveengineering:block/press
                        """).getBytes(StandardCharsets.UTF_8)));
    }
}
