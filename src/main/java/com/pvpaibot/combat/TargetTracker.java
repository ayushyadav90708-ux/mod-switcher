package com.pvpaibot.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Tracks the current combat target: position, validity, distance, etc.
 */
public class TargetTracker {
    private Player target;
    private UUID targetUuid;
    private String targetName;
    private Vec3 lastKnownPos;
    private long lastSeenTick;

    public void setTarget(Player player) {
        if (player == null) {
            clear();
            return;
        }
        this.target = player;
        this.targetUuid = player.getUUID();
        this.targetName = player.getName().getString();
        this.lastKnownPos = player.position();
        this.lastSeenTick = 0;
    }

    public void clear() {
        this.target = null;
        this.targetUuid = null;
        this.targetName = null;
        this.lastKnownPos = null;
        this.lastSeenTick = 0;
    }

    /**
     * Refresh the target reference from the current world.
     * Returns false if the target is no longer available.
     */
    public boolean update(Minecraft client) {
        if (targetUuid == null || client.level == null) {
            clear();
            return false;
        }

        // Prefer existing reference if still valid
        if (target != null && target.isAlive() && !target.isRemoved()
                && target.level() == client.level) {
            lastKnownPos = target.position();
            lastSeenTick = client.level.getGameTime();
            return true;
        }

        // Re-find by UUID
        for (Player p : client.level.players()) {
            if (p.getUUID().equals(targetUuid) && p.isAlive() && !p.isRemoved()) {
                target = p;
                lastKnownPos = p.position();
                lastSeenTick = client.level.getGameTime();
                return true;
            }
        }

        // Target gone
        clear();
        return false;
    }

    public boolean hasTarget() {
        return target != null && targetUuid != null;
    }

    public Player getTarget() {
        return target;
    }

    public String getTargetName() {
        return targetName;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public Vec3 getLastKnownPos() {
        return lastKnownPos;
    }

    public double distanceTo(Player local) {
        if (target == null || local == null) return Double.MAX_VALUE;
        return local.distanceTo(target);
    }

    public Vec3 getEyePosition() {
        if (target == null) return lastKnownPos;
        return target.getEyePosition();
    }
}
