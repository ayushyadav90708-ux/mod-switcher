# PvP AI Bot – Fabric 1.21.11

A **client-side** Minecraft Java Edition mod that provides an AI-controlled combat assistant.  
The bot can automatically fight a selected nearby player using your character and the items in your inventory.

> **Intended use:** singleplayer and private practice environments.  
> Multiplayer automation is **disabled by default** and must be explicitly enabled in code only when the server permits it.

## Features

- Chat commands: `#PVP <PlayerName>`, `#PVP OFF`, `#PVP STATUS`
- Commands are intercepted locally and never sent to the server
- Target tracking among loaded players
- Smooth camera aiming
- Movement toward target while maintaining melee distance
- Attacks respect vanilla attack-cooldown mechanics
- Automatic selection of the best sword/axe in the hotbar
- Auto-eat when health or hunger is low
- Graceful stop on target loss, death, disconnect, or world unload
- Modular architecture ready for future inventory / potion / shield expansions

## Requirements

| Component        | Version          |
|------------------|------------------|
| Minecraft        | 1.21.11          |
| Fabric Loader    | ≥ 0.18.4         |
| Fabric API       | 0.141.x+1.21.11  |
| Java             | 21               |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Download the latest `pvp-ai-*.jar` from the Releases page (or build it yourself).
3. Place the JAR in your `.minecraft/mods` folder together with Fabric API.
4. Launch the game with the Fabric profile.

## Commands

Type these in chat (they will **not** be sent to the server):

| Command              | Description                              |
|----------------------|------------------------------------------|
| `#PVP PlayerName`    | Activate the bot and target that player  |
| `#PVP OFF`           | Stop the bot immediately                 |
| `#PVP STATUS`        | Show whether the bot is active and who it is targeting |

## Building from source

```bash
git clone https://github.com/example/pvp-ai.git
cd pvp-ai
./gradlew build
```

The compiled JAR will be in `build/libs/`.

### Development environment

```bash
./gradlew runClient
```

## Configuration

Runtime options live in `com.pvpaibot.config.ModConfig`:

- `maxPursueDistance` – max blocks the bot will chase
- `preferredMeleeRange` / `minMeleeRange`
- `aimSmoothness`
- `allowMultiplayer` – **false by default**
- `autoSelectWeapon`, `autoEat`, thresholds, etc.

A JSON config file loader can be added later; currently defaults are hard-coded for safety.

## Project structure

```
pvp-ai/
├── build.gradle / settings.gradle / gradle.properties
├── src/main/java/com/pvpaibot/
│   ├── PvpAiClient.java          – mod entry point
│   ├── command/PvpCommandHandler.java
│   ├── combat/
│   │   ├── CombatController.java
│   │   ├── TargetTracker.java
│   │   ├── AimController.java
│   │   └── MovementController.java
│   ├── inventory/InventoryManager.java
│   ├── config/ModConfig.java
│   └── mixin/                    – chat intercept & safety hooks
└── src/main/resources/
    ├── fabric.mod.json
    └── pvp-ai.mixins.json
```

## Completed features

- [x] Chat command system with local interception
- [x] Target selection & tracking
- [x] Smooth aim
- [x] Movement / distance maintenance
- [x] Vanilla-cooldown-aware attacking
- [x] Best melee weapon selection (hotbar swords & axes)
- [x] Auto-eat on low health/hunger
- [x] Stop on target loss / death / disconnect
- [x] Multiplayer disabled by default
- [x] Gradle build + GitHub Actions workflow

## Limitations / future work

- Shield auto-block timing is not implemented (detection only).
- Armor auto-equip and potion auto-use are scaffolded but not fully automated.
- No pathfinding around obstacles (simple directional movement).
- No critical-hit or advanced combo logic.
- Mixin method names for player-remove / disconnect are soft (`require = 0`); the tick-based safety net still stops the bot reliably.
- Icon placeholder (`assets/pvp-ai/icon.png`) is missing; the mod still loads without it.

## Safety & ethics

This mod uses only ordinary client controls and Minecraft client APIs.  
It does **not** implement anti-cheat bypasses, packet spoofing, or hidden automation.

Use only in environments where automated combat is allowed (singleplayer, private servers that have given explicit permission).  
Using automation on public multiplayer servers may violate their rules and can result in bans.

## License

MIT – see [LICENSE](LICENSE).
