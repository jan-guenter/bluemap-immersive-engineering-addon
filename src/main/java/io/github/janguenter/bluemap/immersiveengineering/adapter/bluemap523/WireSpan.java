/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import java.util.Locale;

/** Validated, canonical external IE wire span in absolute world coordinates. */
record WireSpan(WireKind kind, Point start, Point end) {

    static WireSpan canonical(WireKind kind, Point first, Point second) {
        return compare(first, second) <= 0
                ? new WireSpan(kind, first, second)
                : new WireSpan(kind, second, first);
    }

    private static int compare(Point left, Point right) {
        int x = Double.compare(left.x(), right.x());
        if (x != 0) {
            return x;
        }
        int y = Double.compare(left.y(), right.y());
        return y != 0 ? y : Double.compare(left.z(), right.z());
    }

    /** Absolute three-dimensional point. */
    record Point(double x, double y, double z) {

        Point add(Point other) {
            return new Point(x + other.x, y + other.y, z + other.z);
        }

        Point subtract(Point other) {
            return new Point(x - other.x, y - other.y, z - other.z);
        }

        Point scale(double factor) {
            return new Point(x * factor, y * factor, z * factor);
        }

        double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        Point normalize() {
            double magnitude = length();
            return magnitude == 0D ? new Point(0D, 0D, 0D) : scale(1D / magnitude);
        }

        Point cross(Point other) {
            return new Point(
                    y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x
            );
        }
    }

    /** Exact eight built-in wire types persisted by IE 12.4.2-194. */
    enum WireKind {
        COPPER("COPPER", 0.03125D, 0xb36c3f),
        ELECTRUM("ELECTRUM", 0.03125D, 0xeda045),
        STEEL("STEEL", 0.0625D, 0x6f6f6f),
        STRUCTURE_ROPE("STRUCTURE_ROPE", 0.0625D, 0x967e6d),
        STRUCTURE_STEEL("STRUCTURE_STEEL", 0.0625D, 0x6f6f6f),
        REDSTONE("REDSTONE", 0.03125D, 0xff2f2f),
        COPPER_INSULATED("COPPER_INS", 0.03125D, 0xfaf1de),
        ELECTRUM_INSULATED("ELECTRUM_INS", 0.03125D, 0x9d857a);

        private final String persistedName;
        private final double diameter;
        private final int color;

        WireKind(String persistedName, double diameter, int color) {
            this.persistedName = persistedName;
            this.diameter = diameter;
            this.color = color;
        }

        static WireKind parse(String value) {
            if (value == null) {
                return null;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            for (WireKind kind : values()) {
                if (kind.persistedName.equals(normalized)) {
                    return kind;
                }
            }
            return null;
        }

        double radius() {
            return diameter / 2D;
        }

        float red() {
            return ((color >>> 16) & 0xff) / 255F;
        }

        float green() {
            return ((color >>> 8) & 0xff) / 255F;
        }

        float blue() {
            return (color & 0xff) / 255F;
        }
    }
}
