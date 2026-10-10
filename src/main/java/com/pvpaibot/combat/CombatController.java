package com.pvpaibot.combat;

import com.pvpaibot.config.ModConfig;
import com.pvpaibot.inventory.InventoryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Central combat decision-making loop.
 * Coordinates targeting, aiming, movement, weapon selection and attacks.
 */
public class CombatController {
    private final ModConfig config;
    private final TargetTracker targetTracker;
    private final AimController aimController;
    private final MovementController movementController;
    private final InventoryManager inventoryManager;

    private boolean active = false;
    private String stopReason = null;

    public CombatController(ModConfig config) {
        this.config = config;
        this.targetTracker = new TargetTracker();
        this.aimController = new AimController(config);
        this.movementController = new MovementController(config);
        this.inventoryManager = new InventoryManager(config);
    }

    public void start(Player target) {
        if (target == null) return;
        targetTracker.setTarget(target);
        active = true;
        stopReason = null;
        PvpAiClientLog("Combat started against " + target.getName().getString());
    }

    public void stop(String reason) {
        if (!active) return;
        active = false;
        stopReason = reason;
        targetTracker.clear();
        Minecraft client = Minecraft.getInstance();
        movementController.releaseAll(client);
        // Ensure attack key is released
        if (client.options != null) {
            client.options.keyAttack.setDown(false);
        }
        PvpAiClientLog("Combat stopped: " + reason);
    }

    public boolean isActive() {
        return active;
    }

    public String getTargetName() {
        return targetTracker.getTargetName();
    }

    public ModConfig getConfig() {
        return config;
    }

    public void tick(Minecraft client) {
        if (!active) return;

        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            stop("Player or world null");
            return;
        }

        if (!player.isAlive()) {
            stop("Local player died");
            return;
        }

        // Multiplayer safety (re-check every tick)
        if (!client.isLocalServer() && !config.allowMultiplayer) {
            stop("Multiplayer automation disabled");
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§c[PvP AI] Stopped – multiplayer automation is disabled."), false);
            }
            return;
        }

        // Update / validate target
        if (!targetTracker.update(client)) {
            stop("Target no longer available");
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§e[PvP AI] Target lost or died. Bot stopped."), false);
            }
            return;
        }

        Player target = targetTracker.getTarget();
        if (target == null || !target.isAlive()) {
            stop("Target dead");
            return;
        }

        double dist = targetTracker.distanceTo(player);
        if (dist > config.maxPursueDistance) {
            stop("Target too far");
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§e[PvP AI] Target out of range. Bot stopped."), false);
            }
            return;
        }

        // Inventory / equipment intelligence
        inventoryManager.tick(client);

        // Aim
        boolean onTarget = aimController.aimAt(client, target);

        // Movement
        movementController.tick(client, target);

        // Attack when in range, on target, and cooldown ready
        if (onTarget && dist <= config.preferredMeleeRange + 0.5) {
            tryAttack(client, player, target);
        } else {
            client.options.keyAttack.setDown(false);
        }
    }

    private void tryAttack(Minecraft client, LocalPlayer player, Player target) {
        // Respect vanilla attack cooldown
        float cooldown = player.getAttackStrengthScale(0.5f);
        if (cooldown < config.attackCooldownThreshold) {
            client.options.keyAttack.setDown(false);
            return;
        }

        // Prefer using the interaction manager so cooldown & swing are correct
        if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ehr = (EntityHitResult) client.hitResult;
            if (ehr.getEntity() == target) {
                client.gameMode.attack(player, target);
                player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }

        // Fallback: direct attack if crosshair is not exactly on entity
        // (still uses client GameMode so server sees legitimate interaction)
        if (player.distanceTo(target) <= config.preferredMeleeRange + 0.75) {
            client.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static void PvpAiClientLog(String msg) {
        com.pvpaibot.PvpAiClient.LOGGER.info("[Combat] {}", msg);
    }
}
