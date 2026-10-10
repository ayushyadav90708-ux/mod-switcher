package com.pvpaibot;

import com.pvpaibot.command.PvpCommandHandler;
import com.pvpaibot.combat.CombatController;
import com.pvpaibot.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main client entry point for the PvP AI Bot mod.
 * Registers tick handlers, connection events, and initializes subsystems.
 */
public class PvpAiClient implements ClientModInitializer {
    public static final String MOD_ID = "pvp-ai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static PvpAiClient instance;
    private CombatController combatController;
    private PvpCommandHandler commandHandler;
    private ModConfig config;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing PvP AI Bot mod for Minecraft 1.21.11");

        config = new ModConfig();
        combatController = new CombatController(config);
        commandHandler = new PvpCommandHandler(combatController);

        // Tick the combat AI every client tick while in-game
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        // Clean up when disconnecting or changing worlds
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            combatController.stop("Disconnected from server");
        });

        LOGGER.info("PvP AI Bot initialized. Use #PVP <PlayerName> to start, #PVP OFF to stop, #PVP STATUS for status.");
    }

    private void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            if (combatController.isActive()) {
                combatController.stop("World or player unloaded");
            }
            return;
        }
        combatController.tick(client);
    }

    public static PvpAiClient getInstance() {
        return instance;
    }

    public CombatController getCombatController() {
        return combatController;
    }

    public PvpCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ModConfig getConfig() {
        return config;
    }
}
