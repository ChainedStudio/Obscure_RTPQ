# ObscureRTPQ

ObscureRTPQ is a high-performance, asynchronous matchmaking queue extension designed for modern Spigot and Paper Minecraft servers targeting **Minecraft 1.21+**. 

Operating as an optional companion extension, this plugin introduces a competitive "Crystal PvP" style matchmaking loop. It pairs players automatically via non-blocking collection arrays and utilizes a decoupled, thread-safe teleportation worker to place matched players into identical random coordinates with a customizable structural safety offset.

---

## 🛠️ Features Implemented

* **Thread-Safe Matchmaking:** Built on an isolated `ConcurrentLinkedQueue` sequence inside the `QueueManager` to handle rapid player updates without thread contention or memory leaks.
* **Asynchronous Location Checking (`RtpProvider`):** Offloads demanding world coordinate safety verification to a background `CompletableFuture` thread, keeping heavy chunks and blocks from causing server lag or main-thread TPS drops.
* **Modular Soft-Dependency Architecture:** Dynamically references your main `ObscureTeleport` plugin instance upon startup. If the main plugin is absent, it seamlessly switches to an optimized internal random coordinate generator without throwing errors or breaking lifecycle execution.
* **Isolated Administrator Debug Layer:** Built-in loop test parameters allow an admin to run `/rtpq debug` to toggle a solo testing script. While active, entering the queue immediately executes dummy matchmaking behaviors to verify text strings, audio profiles, and teleport vectors entirely alone.
* **Audio Profiles & Text Localization:** Features an automatic `config.yml` generator with native color translation capabilities (`&`) and active dynamic replacements (such as `%opponent%`).

---

## 📂 File Structure Registry

Ensure your project conforms exactly to this folder layout under your `src/` tree to prevent `ClassNotFoundException` compiler errors:

```text
src/main/
├── java/
│   └── main/
│       ├── ObscureRTPQ.java        # Core lifecycle initialization & listener management
│       ├── command/
│       │   └── QueueCommand.java   # Command evaluation parser & subcommand delegation
│       ├── manager/
│       │   └── QueueManager.java   # Core state array control, matchmaking loops & math offsets
│       └── provider/
│           ├── RtpProvider.java    # Contract definition layer for safe teleportation hooks
│           └── ObscureTeleportsProvider.java # Dedicated integration bridge to ObscureTeleport
└── resources/
    ├── plugin.yml                  # Spigot bootstrap registries, parameters & dependency trees
    └── config.yml                  # File configuration mappings, audios, and messages
