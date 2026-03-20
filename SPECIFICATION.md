# Network Simulator Specification

Project: ITL351 Computer Networks Lab - Semester Project
Submission: 1 (Physical + Data Link layers)

## Language and Build
- Language: Java 22
- Build tool: Maven
- Entry point: `com.ansh.networksim.Main`
- Test framework: JUnit 5

## Scope (Submission 1)
### Physical Layer
- Devices: End device, Hub
- Features:
  - Create devices and connect via `Connection`
  - Send and receive `DataPacket`
  - Hub repeats incoming packets on all ports (broadcast)

### Data Link Layer
- Devices: Bridge (2-port), Switch (multi-port)
- Features:
  - Address learning using MAC table (MAC = sender name)
  - Frame forwarding and flooding for unknown destinations
  - Error control: checksum-based detection
  - Access control: CSMA/CD simulation on a shared medium
  - Flow control: Go-Back-N sliding window protocol

## Data Structures
- `DataPacket`: Physical-layer payload (source, destination, payload)
- `Frame`: Data link layer frame with source MAC, destination MAC, sequence number, payload, checksum, and type (DATA/ACK)
- `Connection`: Point-to-point link used for packet and frame transmission

## Enhancements Beyond Minimum Deliverables
- Scenario-based execution in `Main` instead of one long hardcoded demo block
- Topology validation for invalid device registration, self-connection, and duplicate links
- Improved topology reporting with device count and connection count
- MAC table inspection for both `Switch` and `Bridge`
- Improved CSMA/CD behavior with collision rounds, retry attempts, and bounded retries
- Improved Go-Back-N reporting with sender/receiver summary, retransmission tracking, and final statistics
- Automated tests for network validation, switching/bridging behavior, hub broadcast, CSMA/CD, Go-Back-N, and scenario execution

## Protocols
### Error Control
- Checksum on payload (mod 256 sum) for DATA frames
- Receiver validates checksum and reports errors

### Access Control
- CSMA/CD simulated by detecting multiple transmission requests in the same slot
- Collisions trigger deterministic backoff rounds for demo purposes
- Retry attempts are tracked per transmission request
- Transmissions fail if they exceed the configured retry limit

### Flow Control
- Go-Back-N sliding window
- Sender retransmits from base when an error is detected
- Transmission statistics include total frames sent, retransmissions, and ACK count

## Test Cases (Implemented in `Main`)
1. Two end devices with a dedicated connection (physical layer)
2. Star topology with a hub and five end devices (physical layer)
3. Switch with five end devices (data link layer + address learning)
4. CSMA/CD slot with two simultaneous transmitters (access control)
5. Go-Back-N transfer with an injected error (flow control)
6. Two hub-based star topologies connected by a switch

## Domain Reporting
- Broadcast domains: computed as connected components (no L3 devices present)
- Collision domains: computed by treating switches/bridges as collision boundaries

## Automated Test Coverage
- `NetworkTest`: device validation, lookup validation, self-connection rejection, duplicate-link rejection, valid topology creation
- `ConnectionTest`: endpoint resolution and invalid endpoint detection
- `EndDeviceTest`: disconnected-send validation and ACK handling
- `HubTest`: physical-layer broadcast behavior
- `SwitchTest`: MAC learning visibility
- `BridgeTest`: port limit enforcement and MAC learning visibility
- `SharedMediumTest`: collision detection behavior
- `CsmaCdAccessControlTest`: collision flow across backoff rounds
- `GoBackNProtocolTest`: retransmission and successful recovery after injected error
- `MainTest`: scenario runner execution

## Notes / Future Extensions
- Line coding visualization, noise models, advanced ARQ, and additional access control protocols can be added in later submissions.
