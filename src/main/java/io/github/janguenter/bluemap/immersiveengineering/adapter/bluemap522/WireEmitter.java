/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.util.Key;

import java.util.List;

/** Emits IE-style crossed, double-sided wire ribbons into a BlueMap tile. */
final class WireEmitter {

    static final Key WIRE_TEXTURE = Key.parse("immersiveengineering:block/wire");

    private static final double EPSILON = 0.000_001D;

    private final int material;

    WireEmitter(TextureGallery textures) {
        material = textures.get(WIRE_TEXTURE);
        if (material <= 0) {
            throw new IllegalStateException("installed IE wire texture is unavailable");
        }
    }

    void emit(
            WireSpan span,
            Vector3i modelMin,
            Vector3i modelMax,
            Vector3i modelAnchor,
            TileModelView target,
            int sunlight,
            int blocklight
    ) {
        List<WireSpan.Point> points = WireCatenary.points(span);
        WireSpan.Point delta = span.end().subtract(span.start());
        WireSpan.Point side = horizontalSide(delta);
        double radius = span.kind().radius();
        for (int index = 0; index < points.size() - 1; index++) {
            WireSpan.Point first = points.get(index);
            WireSpan.Point second = points.get(index + 1);
            if (!intersects(first, second, radius, modelMin, modelMax)) {
                continue;
            }
            WireSpan.Point vertical = first.subtract(second).cross(side).normalize();
            if (vertical.length() < EPSILON) {
                continue;
            }
            ribbon(span, first, second, side.scale(radius), modelAnchor,
                    target, sunlight, blocklight);
            ribbon(span, first, second, vertical.scale(-radius), modelAnchor,
                    target, sunlight, blocklight);
        }
    }

    static boolean intersects(
            WireSpan.Point first,
            WireSpan.Point second,
            double radius,
            Vector3i modelMin,
            Vector3i modelMax
    ) {
        return Math.max(first.x(), second.x()) + radius >= modelMin.getX()
                && Math.min(first.x(), second.x()) - radius <= modelMax.getX() + 1D
                && Math.max(first.z(), second.z()) + radius >= modelMin.getZ()
                && Math.min(first.z(), second.z()) - radius <= modelMax.getZ() + 1D;
    }

    private static WireSpan.Point horizontalSide(WireSpan.Point delta) {
        WireSpan.Point side = new WireSpan.Point(-delta.z(), 0D, delta.x());
        return side.length() < EPSILON
                ? new WireSpan.Point(1D, 0D, 0D)
                : side.normalize();
    }

    private void ribbon(
            WireSpan span,
            WireSpan.Point first,
            WireSpan.Point second,
            WireSpan.Point offset,
            Vector3i anchor,
            TileModelView target,
            int sunlight,
            int blocklight
    ) {
        WireSpan.Point a = local(first.add(offset), anchor);
        WireSpan.Point b = local(second.add(offset), anchor);
        WireSpan.Point c = local(second.subtract(offset), anchor);
        WireSpan.Point d = local(first.subtract(offset), anchor);
        triangle(span, target, a, b, c, sunlight, blocklight,
                0F, 0F, 1F, 0F, 1F, 1F);
        triangle(span, target, a, c, d, sunlight, blocklight,
                0F, 0F, 1F, 1F, 0F, 1F);
        triangle(span, target, d, c, b, sunlight, blocklight,
                0F, 1F, 1F, 1F, 1F, 0F);
        triangle(span, target, d, b, a, sunlight, blocklight,
                0F, 1F, 1F, 0F, 0F, 0F);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void triangle(
            WireSpan span,
            TileModelView target,
            WireSpan.Point first,
            WireSpan.Point second,
            WireSpan.Point third,
            int sunlight,
            int blocklight,
            float u1,
            float v1,
            float u2,
            float v2,
            float u3,
            float v3
    ) {
        int triangle = target.add(1);
        TileModel model = target.getTileModel();
        model.setPositions(triangle,
                (float) first.x(), (float) first.y(), (float) first.z(),
                (float) second.x(), (float) second.y(), (float) second.z(),
                (float) third.x(), (float) third.y(), (float) third.z());
        model.setUvs(triangle, u1, v1, u2, v2, u3, v3);
        model.setMaterialIndex(triangle, material);
        model.setColor(triangle,
                span.kind().red(), span.kind().green(), span.kind().blue());
        model.setAOs(triangle, 1F, 1F, 1F);
        model.setSunlight(triangle, sunlight);
        model.setBlocklight(triangle, blocklight);
    }

    private static WireSpan.Point local(WireSpan.Point point, Vector3i anchor) {
        return new WireSpan.Point(
                point.x() - anchor.getX(),
                point.y() - anchor.getY(),
                point.z() - anchor.getZ()
        );
    }
}
