/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/** Bounded, read-only loader for IE's NeoForge world wire attachment. */
final class WireAttachmentReader {

    static final String RELATIVE_PATH = "data/neoforge_data_attachments.dat";
    static final int MAX_LOCAL_NETWORKS = 65_536;
    static final int MAX_WIRES = 65_536;

    private static final long RECHECK_NANOS = 500_000_000L;
    private static final long MAX_COMPRESSED_BYTES = 32L * 1024L * 1024L;
    private static final double MAX_OFFSET = 16D;
    private static final double MAX_SPAN = 4_096D;
    private static final int MAX_DIAGNOSTICS = 4;
    private static final Map<Path, CachedNetwork> CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger DIAGNOSTICS = new AtomicInteger();

    List<WireSpan> read(Path worldFolder) {
        Path path = worldFolder.toAbsolutePath().normalize().resolve(RELATIVE_PATH);
        return CACHE.compute(path, WireAttachmentReader::refresh).spans();
    }

    static List<WireSpan> normalize(WireNetworkData root) {
        if (root == null || root.data() == null || root.data().network() == null
                || root.data().network().locals() == null) {
            return List.of();
        }
        List<WireNetworkData.LocalNetwork> locals = root.data().network().locals();
        if (locals.size() > MAX_LOCAL_NETWORKS) {
            return List.of();
        }

        Set<WireSpan> spans = new LinkedHashSet<>();
        int visited = 0;
        for (WireNetworkData.LocalNetwork local : locals) {
            if (local == null || local.wires() == null) {
                continue;
            }
            for (WireNetworkData.Wire wire : local.wires()) {
                if (++visited > MAX_WIRES) {
                    return List.of();
                }
                WireSpan span = normalize(wire);
                if (span != null) {
                    spans.add(span);
                }
            }
        }
        return List.copyOf(spans);
    }

    private static CachedNetwork refresh(Path path, CachedNetwork previous) {
        long checkedAt = System.nanoTime();
        if (previous != null && checkedAt - previous.checkedAt() < RECHECK_NANOS) {
            return previous;
        }
        try {
            if (!Files.isRegularFile(path)) {
                return new CachedNetwork(checkedAt, -1L, -1L, List.of());
            }
            long modified = Files.getLastModifiedTime(path).toMillis();
            long size = Files.size(path);
            if (previous != null && previous.modified() == modified && previous.size() == size) {
                return new CachedNetwork(checkedAt, modified, size, previous.spans());
            }
            if (size <= 0L || size > MAX_COMPRESSED_BYTES) {
                throw new IOException("wire attachment size is outside bounds");
            }
            return new CachedNetwork(checkedAt, modified, size, readAttachment(path));
        } catch (IOException | RuntimeException exception) {
            report(exception);
            List<WireSpan> fallback = previous == null ? List.of() : previous.spans();
            return new CachedNetwork(checkedAt, -2L, -2L, fallback);
        }
    }

    static List<WireSpan> readAttachment(Path path) throws IOException {
        try (InputStream raw = new BufferedInputStream(Files.newInputStream(path));
             InputStream decompressed = new GZIPInputStream(raw)) {
            BlueNBT blueNbt = MCAUtil.addCommonNbtSettings(new BlueNBT());
            return normalize(blueNbt.read(decompressed, WireNetworkData.class));
        }
    }

    private static WireSpan normalize(WireNetworkData.Wire wire) {
        if (wire == null || wire.internal() != 0) {
            return null;
        }
        WireSpan.WireKind kind = WireSpan.WireKind.parse(wire.type());
        WireSpan.Point start = point(wire.endA(), wire.endAOffset());
        WireSpan.Point end = point(wire.endB(), wire.endBOffset());
        if (kind == null || start == null || end == null) {
            return null;
        }
        double length = end.subtract(start).length();
        if (!Double.isFinite(length) || length < 0.001D || length > MAX_SPAN) {
            return null;
        }
        return WireSpan.canonical(kind, start, end);
    }

    private static WireSpan.Point point(
            WireNetworkData.Endpoint endpoint,
            List<Double> offset
    ) {
        if (endpoint == null || offset == null || offset.size() != 3) {
            return null;
        }
        int[] position = endpoint.position();
        if (position == null || position.length != 3) {
            return null;
        }
        double offsetX = value(offset.get(0));
        double offsetY = value(offset.get(1));
        double offsetZ = value(offset.get(2));
        if (!validOffset(offsetX) || !validOffset(offsetY) || !validOffset(offsetZ)) {
            return null;
        }
        return new WireSpan.Point(
                position[0] + offsetX,
                position[1] + offsetY,
                position[2] + offsetZ
        );
    }

    private static double value(Double value) {
        return value == null ? Double.NaN : value;
    }

    private static boolean validOffset(double value) {
        return Double.isFinite(value) && Math.abs(value) <= MAX_OFFSET;
    }

    private static void report(Exception exception) {
        if (DIAGNOSTICS.incrementAndGet() <= MAX_DIAGNOSTICS) {
            System.err.println("BlueMap Immersive Engineering add-on: wire attachment read "
                    + "failed-" + exception.getClass().getSimpleName() + '.');
        }
    }

    private record CachedNetwork(
            long checkedAt,
            long modified,
            long size,
            List<WireSpan> spans
    ) {
    }
}
