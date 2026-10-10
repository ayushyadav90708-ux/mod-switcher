package com.pvpaibot.combat;

import com.pvpaibot.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Smoothly aims the local player's camera at the target's eye position.
 * Uses ordinary client rotation setters (no packet spoofing).
 */
public class AimController {
    private final ModConfig config;

    public AimController(ModConfig config) {
        this.config = config;
    }

    /**
     * Rotate the local player's view toward the target.
     * @return true if the aim is close enough to attack
     */
    public boolean aimAt(Minecraft client, Player target) {
        LocalPlayer player = client.player;
        if (player == null || target == null) return false;

        Vec3 eye = player.getEyePosition();
        Vec3 targetEye = target.getEyePosition();
        Vec3 diff = targetEye.subtract(eye);

        double dist = diff.horizontalDistance();
        float targetYaw = (float) (Mth.atan2(diff.z, diff.x) * (180.0 / Math.PI)) - 90.0f;
        float targetPitch = (float) -(Mth.atan2(diff.y, dist) * (180.0 / Math.PI));

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        float yawDiff = Mth.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float smoothness = Mth.clamp(config.aimSmoothness, 0.05f, 1.0f);
        float newYaw = currentYaw + yawDiff * smoothness;
        float newPitch = Mth.clamp(currentPitch + pitchDiff * smoothness, -90.0f, 90.0f);

        player.setYRot(newYaw);
        player.setXRot(newPitch);
        // Also update the visual rotation used for rendering
        player.yRotO = newYaw;
        player.xRotO = newPitch;

        // Consider "on target" when remaining angular error is small
        return Math.abs(yawDiff) < 8.0f && Math.abs(pitchDiff) < 8.0f;
    }
}
