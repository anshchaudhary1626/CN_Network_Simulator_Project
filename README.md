# Network Simulator: Entire Protocol Stack

ITL351 Computer Networks Lab semester project implemented in Java with Maven.

The simulator builds a predefined network topology in code and runs one automatic full-stack demo. It does not ask the user to select layers from a terminal menu. This matches the project goal of behaving like a real simulator where topology, addresses, routes, ports, and application services are configured before traffic is generated.

## How to Run

Run tests:

```bash
mvn test
```

Build the project:

```bash
mvn clean package
```

Run the automatic simulator:

```bash
mvn exec:java -Dexec.mainClass=com.ansh.networksim.Main
```

If the Maven exec plugin is not configured locally, run after packaging with:

```bash
java -cp target/classes com.ansh.networksim.Main
```

## Topology

```text
HostA                 Router1              Router2                 ServerB
192.168.1.10/24       g0/0 192.168.1.1     g0/0 192.168.2.1        192.168.2.10/24
00:AA:00:00:00:01     s0/0 10.0.0.1/30     s0/0 10.0.0.2/30        00:BB:00:00:00:10
   |                      |                    |                         |
Switch1 ------------------+--------------------+--------------------- Switch2
```

Logical path:

```text
HostA -- Switch1 -- Router1 -- Router2 -- Switch2 -- ServerB
```

## Implemented Features

Physical layer:
- Simulated signal transmission logs for each hop.
- Existing Submission 1 physical-layer classes remain available.

Data link layer:
- Frame creation with source MAC, destination MAC, sequence number, payload, and checksum.
- Existing switch, hub, bridge, CSMA/CD, and data-link Go-Back-N functionality remains available.
- ARP-assisted next-hop MAC resolution is shown before frame transmission.

Network layer:
- `IPv4Address` validates CIDR notation such as `192.168.1.10/24`.
- Network address, subnet mask, and same-network checks.
- Routers with multiple interfaces.
- Static routes.
- RIP route exchange using hop count.
- Routing table lookup using Longest Prefix Match.
- IPv4 packet creation with source IP, destination IP, TTL, protocol field, and payload.

Transport layer:
- Well-known ports for DNS `53` and HTTP `80`.
- Ephemeral client ports from `49152` upward.
- Process-to-process communication endpoints.
- Transport-layer Go-Back-N with sliding window, ACK logs, simulated segment loss, timeout, and retransmission.

Application layer:
- DNS-like service resolving `www.college.local` to `192.168.2.10`.
- HTTP-like service responding to `GET /index.html`.
- Application client that performs DNS resolution before HTTP request flow.

## Automatic Simulation Flow

`com.ansh.networksim.Main` only starts `FullStackSimulation`.

The simulator automatically:

1. Creates HostA, Switch1, Router1, Router2, Switch2, and ServerB.
2. Assigns MAC addresses and classless IPv4 addresses.
3. Configures router interfaces.
4. Installs static routes.
5. Runs RIP route exchange between routers.
6. Prints routing tables.
7. Starts DNS and HTTP services on ServerB.
8. Assigns well-known and ephemeral ports.
9. Sends a DNS query from HostA to ServerB.
10. Sends an HTTP request after DNS resolution.
11. Shows application data becoming transport segments.
12. Shows transport segments becoming IPv4 packets.
13. Shows IPv4 packets becoming data-link frames.
14. Shows physical hop-by-hop transmission.
15. Shows longest-prefix-match routing decisions.
16. Shows receiver-side decapsulation.
17. Prints the final application response received by HostA.

The console output uses structured tags such as:

```text
[APP-DNS] Query: www.college.local
[TRANSPORT] TCP-like Go-Back-N segment created: seq=1
[NETWORK] IPv4 packet created: srcIP=192.168.1.10, destIP=192.168.2.10
[DATALINK] Frame created: srcMAC=..., destMAC=...
[PHYSICAL] Signal transmitted from HostA to Switch1
[ROUTER] Router1 performs longest prefix match for destination 192.168.2.10
[RIP] Router1 sends routing update to Router2
[APPLICATION] Client received response: ...
```

## Tests

JUnit tests cover:

1. IPv4 CIDR validation.
2. Same-network checking.
3. Longest Prefix Match.
4. ARP resolution.
5. Static route lookup.
6. RIP route update.
7. Port assignment.
8. Go-Back-N retransmission.
9. DNS service response.
10. HTTP service response.

Existing Submission 1 tests for physical and data-link behavior are preserved.

## No Menu-Based Layer Selection

The simulator intentionally does not use `Scanner` prompts such as "Press 1 for Physical Layer" or "Enter choice". A network simulator should execute configured scenarios over a topology, so this project runs all layer functions together in one automatic demonstration of the complete protocol stack.
