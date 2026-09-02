/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class WireAttachmentReaderTest {

    @Test
    void readsObservedStagingAttachmentWhenProvided() throws Exception {
        String fixture = System.getenv("IE_WIRE_ATTACHMENT");
        assumeTrue(fixture != null && Files.isRegularFile(Path.of(fixture)));

        List<WireSpan> spans = WireAttachmentReader.readAttachment(Path.of(fixture));

        assertEquals(8, spans.size());
    }

    @Test
    void normalizesExternalWireAndDropsInternalUnknownAndDuplicateRecords() {
        WireNetworkData.Wire external = wire(
                new int[]{174, 106, 56},
                List.of(0.5D, 0.484375D, 0.5D),
                new int[]{160, 104, 56},
                List.of(0.5D, 0.484375D, 0.5D),
                (byte) 0,
                "COPPER"
        );
        WireNetworkData.Wire reverseDuplicate = wire(
                new int[]{160, 104, 56},
                List.of(0.5D, 0.484375D, 0.5D),
                new int[]{174, 106, 56},
                List.of(0.5D, 0.484375D, 0.5D),
                (byte) 0,
                "COPPER"
        );
        WireNetworkData.Wire internal = wire(
                new int[]{1, 2, 3}, List.of(0.5D, 0.5D, 0.5D),
                new int[]{2, 2, 3}, List.of(0.5D, 0.5D, 0.5D),
                (byte) 1, "COPPER"
        );
        WireNetworkData.Wire unknown = wire(
                new int[]{1, 2, 3}, List.of(0.5D, 0.5D, 0.5D),
                new int[]{2, 2, 3}, List.of(0.5D, 0.5D, 0.5D),
                (byte) 0, "THIRD_PARTY_WIRE"
        );
        WireNetworkData root = root(List.of(external, reverseDuplicate, internal, unknown));

        List<WireSpan> spans = WireAttachmentReader.normalize(root);

        assertEquals(1, spans.size());
        WireSpan span = spans.get(0);
        assertEquals(WireSpan.WireKind.COPPER, span.kind());
        assertEquals(new WireSpan.Point(160.5D, 104.484375D, 56.5D), span.start());
        assertEquals(new WireSpan.Point(174.5D, 106.484375D, 56.5D), span.end());
    }

    @Test
    void acceptsAllEightExactPersistedTypeNames() {
        for (WireSpan.WireKind kind : WireSpan.WireKind.values()) {
            assertTrue(kind.radius() > 0D);
            assertTrue(kind.red() >= 0F && kind.red() <= 1F);
            assertTrue(kind.green() >= 0F && kind.green() <= 1F);
            assertTrue(kind.blue() >= 0F && kind.blue() <= 1F);
        }
        assertEquals(WireSpan.WireKind.COPPER_INSULATED,
                WireSpan.WireKind.parse("COPPER_INS"));
        assertEquals(WireSpan.WireKind.ELECTRUM_INSULATED,
                WireSpan.WireKind.parse("ELECTRUM_INS"));
    }

    @Test
    void dropsMalformedOffsetsWithoutHidingValidWire() {
        WireNetworkData.Wire malformed = wire(
                new int[]{1, 2, 3}, List.of(Double.NaN, 0.5D, 0.5D),
                new int[]{2, 2, 3}, List.of(0.5D, 0.5D, 0.5D),
                (byte) 0, "COPPER"
        );
        WireNetworkData.Wire valid = wire(
                new int[]{4, 5, 6}, List.of(0.5D, 0.5D, 0.5D),
                new int[]{8, 5, 6}, List.of(0.5D, 0.5D, 0.5D),
                (byte) 0, "STEEL"
        );

        assertEquals(1, WireAttachmentReader.normalize(root(List.of(malformed, valid))).size());
    }

    private static WireNetworkData root(List<WireNetworkData.Wire> wires) {
        return new WireNetworkData(new WireNetworkData.Data(
                new WireNetworkData.Network(List.of(
                        new WireNetworkData.LocalNetwork(wires)
                ))
        ));
    }

    private static WireNetworkData.Wire wire(
            int[] endA,
            List<Double> offsetA,
            int[] endB,
            List<Double> offsetB,
            byte internal,
            String type
    ) {
        return new WireNetworkData.Wire(
                new WireNetworkData.Endpoint(0, endA),
                new WireNetworkData.Endpoint(0, endB),
                offsetA,
                offsetB,
                internal,
                type
        );
    }
}
