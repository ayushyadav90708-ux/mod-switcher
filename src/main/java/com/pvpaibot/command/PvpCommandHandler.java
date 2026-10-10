package com.pvpaibot.command;

import com.pvpaibot.combat.CombatController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

/**
 * Parses and handles local chat commands:
 *   #PVP PlayerName  – activate bot targeting the named player
 *   #PVP OFF         – stop the bot
 *   #PVP STATUS      – show current state
 *
 * Commands are intercepted client-side and never sent to the server.
 */
public class PvpCommandHandler {
    private final CombatController combatController;

    public PvpCommandHandler(CombatController combatController) {
        this.combatController = combatController;
    }

    /**
     * Attempt to handle a chat message as a PvP command.
     * @param message the raw chat string (without leading slash)
     * @return true if the message was a recognized command and should be cancelled
     */
    public boolean handleChat(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String trimmed = message.trim();
        if (!trimmed.toUpperCase(Locale.ROOT).startsWith("#PVP")) {
            return false;
        }

        String[] parts = trimmed.split("\\s+", 2);
        String sub = parts.length > 1 ? parts[1].trim() : "";

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return true; // cancel anyway
        }

        if (sub.isEmpty() || sub.equalsIgnoreCase("STATUS") || sub.equalsIgnoreCase("?")) {
            sendStatus(player);
            return true;
        }

        if (sub.equalsIgnoreCase("OFF") || sub.equalsIgnoreCase("STOP") || sub.equalsIgnoreCase("DISABLE")) {
            if (combatController.isActive()) {
                combatController.stop("Stopped by command");
                player.displayClientMessage(Component.literal("§a[PvP AI] Bot stopped."), false);
            } else {
                player.displayClientMessage(Component.literal("§e[PvP AI] Bot is already inactive."), false);
            }
            return true;
        }

        // Treat remaining text as player name
        String targetName = sub;
        if (targetName.startsWith("@")) {
            targetName = targetName.substring(1);
        }

        Player target = findPlayerByName(client, targetName);
        if (target == null) {
            player.displayClientMessage(
                    Component.literal("§c[PvP AI] Player \"" + targetName + "\" not found nearby."), false);
            return true;
        }

        if (target == player) {
            player.displayClientMessage(Component.literal("§c[PvP AI] Cannot target yourself."), false);
            return true;
        }

        // Multiplayer safety check
        if (!client.isLocalServer() && !combatController.getConfig().allowMultiplayer) {
            player.displayClientMessage(
                    Component.literal("§c[PvP AI] Multiplayer automation is disabled by default. " +
                            "Enable only on servers that explicitly permit it (edit ModConfig.allowMultiplayer)."), false);
            return true;
        }

        combatController.start(target);
        player.displayClientMessage(
                Component.literal("§a[PvP AI] Now targeting §f" + target.getName().getString() + "§a."), false);
        return true;
    }

    private void sendStatus(LocalPlayer player) {
        if (combatController.isActive()) {
            String name = combatController.getTargetName();
            player.displayClientMessage(
                    Component.literal("§a[PvP AI] Active – targeting §f" + (name != null ? name : "unknown") + "§a."), false);
        } else {
            player.displayClientMessage(Component.literal("§e[PvP AI] Inactive."), false);
        }
    }

    private Player findPlayerByName(Minecraft client, String name) {
        if (client.level == null) return null;
        String lower = name.toLowerCase(Locale.ROOT);
        for (Player p : client.level.players()) {
            if (p.getName().getString().equalsIgnoreCase(name) ||
                    p.getGameProfile().getName().equalsIgnoreCase(name) ||
                    p.getName().getString().toLowerCase(Locale.ROOT).contains(lower)) {
                return p;
            }
        }
        return null;
    }
}
