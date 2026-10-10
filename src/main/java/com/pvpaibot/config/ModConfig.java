package com.pvpaibot.config;

/**
 * Configuration for the PvP AI Bot.
 * Defaults prioritize singleplayer / private practice safety.
 * Multiplayer automation is disabled by default.
 */
public class ModConfig {
    /** Maximum distance at which the bot will pursue a target (blocks). */
    public double maxPursueDistance = 32.0;

    /** Preferred melee attack range (blocks). */
    public double preferredMeleeRange = 3.0;

    /** Minimum distance to maintain from target (blocks). */
    public double minMeleeRange = 1.5;

    /** How smoothly the camera aims (lower = smoother, 0.05–0.4 typical). */
    public float aimSmoothness = 0.25f;

    /** Whether the bot is allowed to run on multiplayer servers. Default: false. */
    public boolean allowMultiplayer = false;

    /** Whether to automatically select the best melee weapon in the hotbar. */
    public boolean autoSelectWeapon = true;

    /** Whether to attempt to eat food when health or hunger is low. */
    public boolean autoEat = true;

    /** Health threshold (absolute) below which auto-eat triggers. */
    public float eatHealthThreshold = 12.0f; // 6 hearts

    /** Hunger threshold below which auto-eat triggers. */
    public int eatHungerThreshold = 14;

    /** Attack cooldown tolerance (fraction of full cooldown required before attacking). */
    public float attackCooldownThreshold = 0.9f;

    public ModConfig() {
        // Defaults are set above. Future: load from JSON config file.
    }
}
