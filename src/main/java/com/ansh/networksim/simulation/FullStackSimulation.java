package com.ansh.networksim.simulation;

import com.ansh.networksim.application.ApplicationClient;
import com.ansh.networksim.application.DNSService;
import com.ansh.networksim.application.HTTPService;
import com.ansh.networksim.core.SimulatedHost;
import com.ansh.networksim.datalink.Frame;
import com.ansh.networksim.network.ARPTable;
import com.ansh.networksim.network.IPv4Packet;
import com.ansh.networksim.network.RouteEntry;
import com.ansh.networksim.transport.GoBackNTransport;
import com.ansh.networksim.transport.PortManager;
import com.ansh.networksim.transport.Process;
import com.ansh.networksim.transport.TransportSegment;

import java.util.List;

/**
 * Runs the complete automatic protocol-stack demonstration.
 *
 * In simple words, this is the "story" of the simulator:
 * - Build the network.
 * - Start DNS and HTTP services.
 * - Give ports to client/server processes.
 * - Send DNS and HTTP messages.
 * - Show how data is wrapped layer by layer.
 * - Show how routers forward it.
 * - Show how the receiver unwraps it.
 */
public class FullStackSimulation {
    private final NetworkTopologyBuilder topologyBuilder;
    private final ARPTable router2ArpTable = new ARPTable("Router2");

    public FullStackSimulation() {
        // Default constructor uses the standard topology builder.
        this(new NetworkTopologyBuilder());
    }

    public FullStackSimulation(NetworkTopologyBuilder topologyBuilder) {
        // This constructor helps tests or future demos provide a different builder.
        this.topologyBuilder = topologyBuilder;
    }

    public void run() {
        // This method is called by Main. It runs everything automatically.
        System.out.println("===== ITL351 FULL PROTOCOL STACK NETWORK SIMULATION =====");

        // Step 1: create hosts, routers, IPs, MACs, routes, and RIP-learned routes.
        NetworkTopologyBuilder.Topology topology = topologyBuilder.build();
        topology.router1().getRoutingTable().print(topology.router1().getName());
        topology.router2().getRoutingTable().print(topology.router2().getName());

        // Step 2: create application services and assign transport ports.
        PortManager portManager = new PortManager();
        DNSService dnsService = new DNSService();
        HTTPService httpService = new HTTPService();
        int dnsPort = portManager.assignWellKnownPort("ServerB-DNS", dnsService.getPort());
        int httpPort = portManager.assignWellKnownPort("ServerB-HTTP", httpService.getPort());
        int clientDnsPort = portManager.assignEphemeralPort("HostA-DNS-Client");
        int clientHttpPort = portManager.assignEphemeralPort("HostA-HTTP-Client");

        Process dnsProcess = new Process("ServerB-DNS", dnsPort, topology.serverB());
        Process httpProcess = new Process("ServerB-HTTP", httpPort, topology.serverB());
        // These logs show process-to-process communication, not only computer-to-computer communication.
        System.out.println("[TRANSPORT] Process-to-process endpoints ready: "
                + topology.hostA().getName() + ":" + clientDnsPort + " -> "
                + dnsProcess.getHost().getName() + ":" + dnsProcess.getPortNumber());
        System.out.println("[TRANSPORT] Process-to-process endpoints ready: "
                + topology.hostA().getName() + ":" + clientHttpPort + " -> "
                + httpProcess.getHost().getName() + ":" + httpProcess.getPortNumber());

        // Step 3: DNS flow. Client asks for the server's IP address.
        ApplicationClient client = new ApplicationClient();
        String dnsQuery = client.createDnsQuery("www.college.local");
        sendApplicationMessage(topology, topology.hostA(), topology.serverB(), clientDnsPort, dnsPort, dnsQuery, true);
        String resolvedIp = dnsService.handleRequest(dnsQuery);
        System.out.println("[APPLICATION] DNS response received by client: www.college.local -> " + resolvedIp);

        // Step 4: HTTP flow. Client sends GET request after DNS resolution.
        String httpRequest = client.createHttpGet("/index.html");
        sendApplicationMessage(topology, topology.hostA(), topology.serverB(), clientHttpPort, httpPort, httpRequest, false);
        String response = httpService.handleRequest(httpRequest);
        System.out.println("[APPLICATION] Client received response: " + response.replace('\n', ' '));

        System.out.println("===== SIMULATION COMPLETE =====");
    }

    private void sendApplicationMessage(NetworkTopologyBuilder.Topology topology,
                                        SimulatedHost source,
                                        SimulatedHost destination,
                                        int sourcePort,
                                        int destinationPort,
                                        String applicationPayload,
                                        boolean simulateLoss) {
        // This method shows application data entering the transport layer.
        System.out.println("[APPLICATION] Application Data: " + applicationPayload);

        // Split the application message into smaller chunks so Go-Back-N has segments to send.
        GoBackNTransport goBackN = new GoBackNTransport(3);
        List<String> chunks = chunkPayload(applicationPayload);
        // DNS flow deliberately loses one segment so retransmission is visible in logs.
        int lostSequence = simulateLoss && chunks.size() > 1 ? 1 : -1;
        List<TransportSegment> deliveredSegments = goBackN.sendSegments(sourcePort, destinationPort, chunks, lostSequence);

        // Each delivered transport segment is wrapped into network/data-link/physical layers.
        for (TransportSegment segment : deliveredSegments) {
            encapsulateAndTransmit(topology, source, destination, segment);
        }
        System.out.println("[TRANSPORT] Receiver reassembled transport payload: " + applicationPayload);
    }

    private List<String> chunkPayload(String payload) {
        // Break long text into 8-character pieces to simulate multiple transport segments.
        return java.util.stream.IntStream.iterate(0, index -> index < payload.length(), index -> index + 8)
                .mapToObj(index -> payload.substring(index, Math.min(index + 8, payload.length())))
                .toList();
    }

    private void encapsulateAndTransmit(NetworkTopologyBuilder.Topology topology,
                                        SimulatedHost source,
                                        SimulatedHost destination,
                                        TransportSegment segment) {
        // Transport layer has created a segment. Now the network layer wraps it in an IPv4 packet.
        System.out.println("[TRANSPORT] Segment ready: " + segment);

        IPv4Packet packet = new IPv4Packet(source.getIpOnly(), destination.getIpOnly(), 64, "GBN", segment.toString());
        System.out.println("[NETWORK] IPv4 packet created: srcIP=" + packet.getSourceIp()
                + ", destIP=" + packet.getDestinationIp()
                + ", ttl=" + packet.getTtl()
                + ", protocol=" + packet.getProtocol());

        // HostA cannot directly send to ServerB's MAC because ServerB is on another network.
        // So HostA uses ARP to find its default gateway Router1.
        String router1Mac = source.getArpTable().resolve(
                source.getIpAddress(),
                source.getDefaultGatewayIp(),
                source.getMacAddress(),
                topology.hostALan()
        );
        // Data-link layer creates a frame for the first hop: HostA -> Router1.
        Frame firstHopFrame = Frame.createDataFrame(source.getMacAddress(), router1Mac, segment.getSequenceNumber(), packet.toString());
        System.out.println("[DATALINK] Frame created: srcMAC=" + firstHopFrame.getSourceMac()
                + ", destMAC=" + firstHopFrame.getDestinationMac()
                + ", seq=" + firstHopFrame.getSequenceNumber());
        System.out.println("[PHYSICAL] Signal transmitted from HostA to Switch1");
        System.out.println("[PHYSICAL] Signal transmitted from Switch1 to Router1");

        // Router1 checks its routing table using Longest Prefix Match.
        RouteEntry router1Route = topology.router1().forward(packet)
                .orElseThrow(() -> new IllegalStateException("Router1 has no route to " + destination.getIpOnly()));
        System.out.println("[NETWORK] Router1 forwards packet through " + router1Route.getOutgoingInterface());
        System.out.println("[PHYSICAL] Signal transmitted from Router1 to Router2");

        // Packet has crossed one router, so TTL decreases.
        IPv4Packet routedPacket = packet.decrementTtl();
        // Router2 now decides how to reach ServerB's LAN.
        RouteEntry router2Route = topology.router2().forward(routedPacket)
                .orElseThrow(() -> new IllegalStateException("Router2 has no route to " + destination.getIpOnly()));
        System.out.println("[NETWORK] Router2 forwards packet through " + router2Route.getOutgoingInterface());

        // Router2 is on ServerB's LAN, so it uses ARP to find ServerB's MAC address.
        String serverMac = router2ArpTable.resolve(
                topology.router2().getInterface("g0/0").getIpAddress(),
                destination.getIpOnly(),
                topology.router2().getInterface("g0/0").getMacAddress(),
                topology.serverBLan()
        );
        // Data-link layer creates the final local frame: Router2 -> ServerB.
        Frame finalFrame = Frame.createDataFrame(topology.router2().getInterface("g0/0").getMacAddress(), serverMac, segment.getSequenceNumber(), routedPacket.toString());
        System.out.println("[DATALINK] Frame created: srcMAC=" + finalFrame.getSourceMac()
                + ", destMAC=" + finalFrame.getDestinationMac()
                + ", seq=" + finalFrame.getSequenceNumber());
        System.out.println("[PHYSICAL] Signal transmitted from Router2 to Switch2");
        System.out.println("[PHYSICAL] Signal transmitted from Switch2 to ServerB");

        // Receiver side: ServerB unwraps the frame, then packet, then segment.
        System.out.println("[DATALINK] ServerB decapsulates data-link frame from " + finalFrame.getSourceMac());
        System.out.println("[NETWORK] ServerB decapsulates IPv4 packet for " + routedPacket.getDestinationIp());
        System.out.println("[TRANSPORT] ServerB accepts segment for port " + segment.getDestinationPort()
                + " and sequence " + segment.getSequenceNumber());
    }
}
