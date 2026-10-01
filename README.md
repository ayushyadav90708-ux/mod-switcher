# Mod Switcher (Fabric, Minecraft Java 1.21.11)

Client-side mod. Mod id `modswitcher`, version `1.0.0`. Needs Fabric Loader, Fabric API and Java 21.

## What it does

- Adds a **Mod Switcher** button (top-left) to the **title screen** and the **pause menu**.
- Opens a **paged list of loaded mods** (name, id, version).
- **Disable / Enable** buttons queue a change. **Nothing changes in the running game**:
  Fabric cannot safely unload mods mid-session, so every change needs a **restart**. The screen says so in orange at the top.
- Queued changes are applied when the game **closes**: the mod's jar is renamed to `<name>.jar.disabled`
  (or back to `.jar`). On Windows the jar is locked until the game is gone, so a small temporary script finishes the rename.
- **View: Disabled** lists jars already disabled in the mods folder so you can queue them for re-enabling.
- **Libraries: Shown/Hidden** reveals built-in and bundled library mods. They are always locked.
- **Restart Game** (experimental) closes Minecraft, applies the changes, then starts it again with the same settings.
  It rebuilds the game's launch command, so it may not work with every launcher; if the game does not reopen, just start it yourself.
- **Quit Game** closes Minecraft so the changes apply; **Clear Changes** cancels the queue.
- Why not reload instantly like resource packs? Resource packs are only assets and can be re-read at any time. Mods are Java code that
  Fabric loads once at startup, and there is no safe way to unload or add them in a running game.

## Safety rules

- A mod that another loaded mod depends on is locked ("Required by: ...") until those mods are queued for disabling first,
  so the game cannot be left unable to start. Undo works the same way in reverse.
- Mod Switcher cannot disable itself. Built-in and bundled mods are locked.
- Only plain `.jar` files directly inside the `mods` folder are touched.

## Build with no software installed

Same as any GitHub Actions project: upload these files to a new GitHub repository (include the `.github/workflows/build.yml` file),
open the **Actions** tab, wait for the green check, download the **modswitcher-1.0.0** artifact and unzip it to get `modswitcher-1.0.0.jar`.
Put the jar in `.minecraft/mods` with Fabric API.
