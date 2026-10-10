package com.pvpaibot.combat;

import com.pvpaibot.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Controls movement keys to approach / circle the target while
 * maintaining a suitable melee distance. All inputs are released
 * when the AI stops.
 */
public class MovementController {
    private final ModConfig config;
    private boolean forward, back, left, right, jump, sprint;

    public MovementController(ModConfig config) {
        this.config = config;
    }

    public void tick(Minecraft client, Player target) {
        LocalPlayer player = client.player;
        if (player == null || target == null) {
            releaseAll(client);
            return;
        }

        double dist = player.distanceTo(target);
        Vec3 toTarget = target.position().subtract(player.position()).normalize();

        // Desired movement relative to look direction
        float yaw = player.getYRot() * ((float) Math.PI / 180.0f);
        Vec3 lookForward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        Vec3 lookRight = new Vec3(lookForward.z, 0, -lookForward.x);

        double forwardDot = toTarget.dot(lookForward);
        double rightDot = toTarget.dot(lookRight);

        // Reset
        forward = back = left = right = jump = sprint = false;

        if (dist > config.preferredMeleeRange) {
            // Move toward target
            if (forwardDot > 0.2) forward = true;
            else if (forwardDot < -0.2) back = true;

            if (rightDot > 0.3) right = true;
            else if (rightDot < -0.3) left = true;

            sprint = dist > config.preferredMeleeRange + 2.0;
        } else if (dist < config.minMeleeRange) {
            // Too close – back up a little
            back = true;
        } else {
            // In range – slight strafing to stay mobile
            if (client.level != null && client.level.getGameTime() % 40 < 20) {
                left = true;
            } else {
                right = true;
            }
        }

        // Jump if needed to climb or if target is higher
        if (target.getY() > player.getY() + 0.5 && player.onGround()) {
            jump = true;
        }

        apply(client);
    }

    private void apply(Minecraft client) {
        Options options = client.options;
        options.keyUp.setDown(forward);
        options.keyDown.setDown(back);
        options.keyLeft.setDown(left);
        options.keyRight.setDown(right);
        options.keyJump.setDown(jump);
        options.keySprint.setDown(sprint);
    }

    /**
     * Release every movement key the AI may have held.
     */
    public void releaseAll(Minecraft client) {
        if (client == null || client.options == null) return;
        Options options = client.options;
        options.keyUp.setDown(false);
        options.keyDown.setDown(false);
        options.keyLeft.setDown(false);
        options.keyRight.setDown(false);
        options.keyJump.setDown(false);
        options.keySprint.setDown(false);
        forward = back = left = right = jump = sprint = false;
    }
}
