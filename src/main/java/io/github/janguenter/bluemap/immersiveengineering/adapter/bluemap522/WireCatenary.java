/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;

/** Clean-room implementation of IE's fixed 16-section wire catenary. */
final class WireCatenary {

    static final int SEGMENTS = 16;
    static final double SLACK = 1.005D;

    private WireCatenary() {
    }

    static List<WireSpan.Point> points(WireSpan span) {
        WireSpan.Point start = span.start();
        WireSpan.Point delta = span.end().subtract(start);
        double horizontal = Math.hypot(delta.x(), delta.z());
        if (horizontal < 0.05D) {
            return linear(start, delta);
        }

        double length = delta.length() * SLACK;
        double ratioSquared = length * length - delta.y() * delta.y();
        if (!(ratioSquared > 0D)) {
            return linear(start, delta);
        }
        double target = Math.sqrt(ratioSquared) / horizontal;
        double lower = 0D;
        double upper = 1D;
        while (Math.sinh(upper) / upper < target && upper < 64D) {
            lower = upper;
            upper *= 2D;
        }
        for (int iteration = 0; iteration < 20; iteration++) {
            double middle = (lower + upper) / 2D;
            if (Math.sinh(middle) / middle < target) {
                lower = middle;
            } else {
                upper = middle;
            }
        }

        double parameter = (lower + upper) / 2D;
        double scale = horizontal / (2D * parameter);
        double logArgument = (length + delta.y()) / (length - delta.y());
        double offsetX = 0.5D * (horizontal - scale * Math.log(logArgument));
        double offsetY = 0.5D * (
                delta.y() - length * Math.cosh(parameter) / Math.sinh(parameter)
        );
        if (!Double.isFinite(scale) || !Double.isFinite(offsetX)
                || !Double.isFinite(offsetY)) {
            return linear(start, delta);
        }

        List<WireSpan.Point> points = new ArrayList<>(SEGMENTS + 1);
        for (int index = 0; index <= SEGMENTS; index++) {
            if (index == 0) {
                points.add(start);
            } else if (index == SEGMENTS) {
                points.add(span.end());
            } else {
                double progress = index / (double) SEGMENTS;
                double y = scale * Math.cosh(
                        (horizontal * progress - offsetX) / scale
                ) + offsetY;
                points.add(new WireSpan.Point(
                        start.x() + delta.x() * progress,
                        start.y() + y,
                        start.z() + delta.z() * progress
                ));
            }
        }
        return List.copyOf(points);
    }

    private static List<WireSpan.Point> linear(
            WireSpan.Point start,
            WireSpan.Point delta
    ) {
        List<WireSpan.Point> points = new ArrayList<>(SEGMENTS + 1);
        for (int index = 0; index <= SEGMENTS; index++) {
            points.add(start.add(delta.scale(index / (double) SEGMENTS)));
        }
        return List.copyOf(points);
    }
}
