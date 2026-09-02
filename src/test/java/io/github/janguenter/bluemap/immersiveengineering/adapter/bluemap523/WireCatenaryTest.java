/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flowpowered.math.vector.Vector3i;

import java.util.List;

import org.junit.jupiter.api.Test;

class WireCatenaryTest {

    @Test
    void producesExactEndpointsAndSagAcrossSixteenSegments() {
        WireSpan span = WireSpan.canonical(
                WireSpan.WireKind.COPPER,
                new WireSpan.Point(0.5D, 10.5D, 0.5D),
                new WireSpan.Point(14.5D, 8.5D, 0.5D)
        );

        List<WireSpan.Point> points = WireCatenary.points(span);

        assertEquals(17, points.size());
        assertEquals(span.start(), points.get(0));
        assertEquals(span.end(), points.get(16));
        double linearMiddleY = (span.start().y() + span.end().y()) / 2D;
        assertTrue(points.get(8).y() < linearMiddleY);
        assertTrue(points.stream().allMatch(point ->
                Double.isFinite(point.x())
                        && Double.isFinite(point.y())
                        && Double.isFinite(point.z())));
    }

    @Test
    void usesLinearFallbackForVerticalWire() {
        WireSpan span = WireSpan.canonical(
                WireSpan.WireKind.STRUCTURE_STEEL,
                new WireSpan.Point(3.5D, 2D, 4.5D),
                new WireSpan.Point(3.5D, 18D, 4.5D)
        );

        List<WireSpan.Point> points = WireCatenary.points(span);

        assertEquals(new WireSpan.Point(3.5D, 10D, 4.5D), points.get(8));
    }

    @Test
    void segmentIntersectionUsesTileFootprintRatherThanEndpointsOnly() {
        WireSpan.Point first = new WireSpan.Point(15.5D, 10D, 8D);
        WireSpan.Point second = new WireSpan.Point(17.5D, 10D, 8D);

        assertTrue(WireEmitter.intersects(first, second, 0.05D,
                new Vector3i(16, 0, 0), new Vector3i(31, 255, 15)));
    }
}
