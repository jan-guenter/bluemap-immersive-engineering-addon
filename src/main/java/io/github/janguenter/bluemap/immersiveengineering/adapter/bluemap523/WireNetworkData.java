/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.immersiveengineering.adapter.bluemap523;

import de.bluecolored.bluenbt.NBTName;

import java.util.List;

/** Narrow BlueNBT projection of IE's NeoForge world wire-network attachment. */
public final class WireNetworkData {

    @NBTName("data")
    private Data data;

    public WireNetworkData() {
    }

    WireNetworkData(Data data) {
        this.data = data;
    }

    Data data() {
        return data;
    }

    /** NeoForge saved-data payload. */
    public static final class Data {

        @NBTName("immersiveengineering:wire_network")
        private Network network;

        public Data() {
        }

        Data(Network network) {
            this.network = network;
        }

        Network network() {
            return network;
        }
    }

    /** IE global wire network. */
    public static final class Network {

        @NBTName("locals")
        private List<LocalNetwork> locals;

        public Network() {
        }

        Network(List<LocalNetwork> locals) {
            this.locals = locals;
        }

        List<LocalNetwork> locals() {
            return locals;
        }
    }

    /** One persisted local network. */
    public static final class LocalNetwork {

        @NBTName("wires")
        private List<Wire> wires;

        public LocalNetwork() {
        }

        LocalNetwork(List<Wire> wires) {
            this.wires = wires;
        }

        List<Wire> wires() {
            return wires;
        }
    }

    /** One persisted IE connection. */
    public static final class Wire {

        @NBTName("endA")
        private Endpoint endA;

        @NBTName("endB")
        private Endpoint endB;

        @NBTName("endAOffset")
        private List<Double> endAOffset;

        @NBTName("endBOffset")
        private List<Double> endBOffset;

        @NBTName("internal")
        private byte internal;

        @NBTName("type")
        private String type;

        public Wire() {
        }

        Wire(
                Endpoint endA,
                Endpoint endB,
                List<Double> endAOffset,
                List<Double> endBOffset,
                byte internal,
                String type
        ) {
            this.endA = endA;
            this.endB = endB;
            this.endAOffset = endAOffset;
            this.endBOffset = endBOffset;
            this.internal = internal;
            this.type = type;
        }

        Endpoint endA() {
            return endA;
        }

        Endpoint endB() {
            return endB;
        }

        List<Double> endAOffset() {
            return endAOffset;
        }

        List<Double> endBOffset() {
            return endBOffset;
        }

        byte internal() {
            return internal;
        }

        String type() {
            return type;
        }
    }

    /** Persisted connection-point identity. */
    public static final class Endpoint {

        @NBTName("index")
        private int index;

        @NBTName("position")
        private int[] position;

        public Endpoint() {
        }

        Endpoint(int index, int[] position) {
            this.index = index;
            this.position = position == null ? null : position.clone();
        }

        int index() {
            return index;
        }

        int[] position() {
            return position == null ? null : position.clone();
        }
    }
}
