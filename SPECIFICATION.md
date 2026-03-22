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

## File-by-File Walkthrough

### 1. Entry and User Flow
#### `src/main/java/com/ansh/networksim/Main.java`
- Role:
  - Entry point and interactive scenario runner.
- What it does:
  - Accepts command-line scenario names like `csma`, `gobackn`, `visualize`, and `custom`.
  - If no argument is passed, it prompts the user.
  - Builds demo or custom topologies.
  - Accepts user input for:
    - scenario type
    - number of devices
    - sender and receiver
    - payload format: message or raw bits
    - payload data
    - Go-Back-N window size and optional error injection
    - CSMA/CD competing sender when collision demo is requested
- Example run:
```text
Enter scenario: custom
Choose mode [physical/switch/csma/gobackn]: gobackn
How many end devices do you need? 2
Enter sender device [D1/D2]: D1
Enter receiver device [D2]: D2
Payload format [message/bits]: bits
Enter payload: 1010101011110000
Enter Go-Back-N window size: 2
Enter sequence number to corrupt (-1 for no error, max 1): -1
```

### 2. Core Topology Model
#### `src/main/java/com/ansh/networksim/model/Device.java`
- Role:
  - Base class for all devices.
- Stores:
  - `id`
  - `name`
  - list of `Connection`s
- Declares:
  - `receive(DataPacket, Connection)`
  - `receiveFrame(Frame, Connection)`

#### `src/main/java/com/ansh/networksim/model/DataPacket.java`
- Role:
  - Physical-layer message unit.
- Stores:
  - source
  - destination
  - payload

#### `src/main/java/com/ansh/networksim/model/Connection.java`
- Role:
  - Point-to-point wire between two devices.
- Behavior:
  - `transmit(...)` sends physical packets
  - `transmitFrame(...)` sends data-link frames
- Example output:
```text
Physical-layer packet transmitted from D1 to D2.
Data-link frame transmitted from S1 to SW1.
```

#### `src/main/java/com/ansh/networksim/model/EndDevice.java`
- Role:
  - Host node that sends and receives packets and frames.
- Behavior:
  - `send(...)` creates `DataPacket`
  - `sendFrame(...)` creates `Frame`
  - accepts only packets/frames addressed to itself
  - logs ACK reception separately
- Example output:
```text
D1 is sending a physical-layer packet to D2 with payload: Hello
D2 received the physical-layer message: Hello
S1 received ACK for sequence number 2.
```

#### `src/main/java/com/ansh/networksim/model/Hub.java`
- Role:
  - Shared physical repeater.
- Behavior:
  - Repeats incoming packets/frames on all ports except the source port.
- Example output:
```text
H1 received a physical-layer packet and is broadcasting it to all other connections.
```

#### `src/main/java/com/ansh/networksim/model/Switch.java`
- Role:
  - Layer-2 forwarding device with MAC learning.
- Behavior:
  - Learns source MAC on incoming port
  - forwards known destination directly
  - floods unknown destination
- Example output:
```text
SW1 learned source MAC S1 on incoming connection.
SW1 forwarding frame to S3
```

#### `src/main/java/com/ansh/networksim/model/Bridge.java`
- Role:
  - Two-port L2 forwarding device.
- Behavior:
  - Same learning logic as switch
  - rejects more than two connections

### 3. Network Builder
#### `src/main/java/com/ansh/networksim/network/Network.java`
- Role:
  - Topology manager.
- Behavior:
  - registers devices
  - connects devices
  - prints topology
  - counts broadcast domains
  - counts collision domains
- Example output:
```text
Connected A <--> B
--- Network Topology ---
Devices: 2
Connections: 1
Connection{device1=A, device2=B}
```

### 4. Data-Link Structures
#### `src/main/java/com/ansh/networksim/datalink/FrameType.java`
- Role:
  - Distinguishes `DATA` and `ACK`.

#### `src/main/java/com/ansh/networksim/datalink/ChecksumUtil.java`
- Role:
  - Computes a simple modulo-256 checksum over payload characters.

#### `src/main/java/com/ansh/networksim/datalink/Frame.java`
- Role:
  - Data-link transfer unit.
- Stores:
  - source MAC
  - destination MAC
  - sequence number
  - payload
  - checksum
  - frame type
- Behavior:
  - creates data and ACK frames
  - validates checksum
  - can intentionally corrupt payload for demo/testing

#### `src/main/java/com/ansh/networksim/datalink/ErrorInjector.java`
- Role:
  - Corrupts selected sequence numbers once.

#### `src/main/java/com/ansh/networksim/datalink/TransmissionRequest.java`
- Role:
  - Represents one sender attempting to transmit one frame on a shared medium.

### 5. CSMA/CD
#### `src/main/java/com/ansh/networksim/datalink/SharedMedium.java`
- Role:
  - Tick-aware shared medium state.
- Stores:
  - medium name
  - current tick
  - busy/idle status
  - active transmission requests
- Behavior:
  - begins transmission rounds
  - reports collisions
  - advances clock
  - clears occupancy after round completion

#### `src/main/java/com/ansh/networksim/datalink/CsmaCdAccessControl.java`
- Role:
  - Access control algorithm for shared-medium contention.
- Algorithm:
  1. Start a transmission round on `SharedMedium`.
  2. If one sender is active, it transmits successfully.
  3. If multiple senders are active, collision is declared.
  4. Jam signal is shown.
  5. Binary exponential backoff slot is selected for each sender.
  6. Earliest contenders retry first.
  7. If multiple retry together, they collide again.
- Example output:
```text
Collision detected for 2 simultaneous transmissions.
Collision trace on shared medium:
Tick 0: S1 sent payload bit 0, S2 sent payload bit 0 -> collision on medium
S1 detected the collision and is sending a jam signal.
S2 selected backoff slot 0 from range [0, 1].
```

### 6. Go-Back-N
#### `src/main/java/com/ansh/networksim/datalink/GoBackNProtocol.java`
- Role:
  - Sliding-window ARQ simulation.
- Algorithm:
  1. Send all frames in the current window.
  2. Start a timer for each sent frame.
  3. Receiver accepts only the next expected sequence.
  4. Receiver sends cumulative ACKs.
  5. Corrupted or out-of-order frames cause repeated ACKs.
  6. Sender waits until timeout of the first missing frame.
  7. Sender goes back and retransmits from that sequence.
- Example output:
```text
Window send range: [0, 2]
Timer started for frame 0 at tick 0 with timeout threshold tick 120.
S2 accepted frame with sequence number 0.
S2 sent ACK for sequence number 0.
S2 detected an error in frame with sequence number 2.
S1 received cumulative ACKs up to sequence number 1 and will go back to 2 after timeout at tick 446.
```

### 7. Visualization Helpers
#### `src/main/java/com/ansh/networksim/simulation/PayloadUtil.java`
- Role:
  - Converts text payloads and raw bit payloads into payload-bit strings.
- Behavior:
  - text input is converted character-by-character into 8-bit binary
  - raw bit input is preserved exactly

#### `src/main/java/com/ansh/networksim/simulation/BitStream.java`
- Role:
  - Generic bit sequence wrapper for visualization helpers.

#### `src/main/java/com/ansh/networksim/simulation/FrameSerializer.java`
- Role:
  - Converts a full frame into a full serialized bitstream.
- Note:
  - Full frame serialization is kept for extensibility, even though the current visible trace focuses only on payload bits.

#### `src/main/java/com/ansh/networksim/simulation/TransmissionTraceRenderer.java`
- Role:
  - Prints payload bits per tick.
- Behavior:
  - Data frames:
    - prints `Payload bits: ...`
    - prints one payload bit per tick
  - ACK frames:
    - prints `Payload bits: <none>`
  - Collisions:
    - shows only overlapping payload bits
    - then prints jam signal bits
- Example output:
```text
S1 prepared DATA frame seq=0 for S2.
Payload bits: 0101000000110000
Tick 0: S1 -> S2 payload bit 1/16 = 0
Tick 1: S1 -> S2 payload bit 2/16 = 1
```

### 8. Tests
#### `src/test/java/com/ansh/networksim/MainTest.java`
- Verifies:
  - scenario selection
  - interactive prompting
  - custom physical run with bit payload

#### `src/test/java/com/ansh/networksim/datalink/GoBackNProtocolTest.java`
- Verifies:
  - payload-bit tracing
  - timer logs
  - ACK logs
  - retransmission behavior

#### `src/test/java/com/ansh/networksim/datalink/CsmaCdAccessControlTest.java`
- Verifies:
  - collision logging
  - jam signal
  - deterministic backoff

#### `src/test/java/com/ansh/networksim/datalink/SharedMediumTest.java`
- Verifies:
  - busy/idle state
  - collision detection
  - tick advancement

#### `src/test/java/com/ansh/networksim/model/*.java`
- Verifies:
  - end-device behavior
  - connection behavior
  - hub broadcast
  - switch learning
  - bridge port limit

#### `src/test/java/com/ansh/networksim/network/NetworkTest.java`
- Verifies:
  - duplicate device rejection
  - blank names
  - self-connection rejection
  - duplicate link rejection
  - valid topology creation

## Verification Commands
- Build:
```bash
mvn compile
```
- Run tests:
```bash
mvn test
```
- Start interactive mode:
```bash
java -cp target/classes com.ansh.networksim.Main
```
