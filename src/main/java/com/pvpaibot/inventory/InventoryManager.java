package com.pvpaibot.inventory;

import com.pvpaibot.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Modular inventory intelligence.
 *
 * Implemented features:
 *  - Best melee weapon selection (swords & axes) in the hotbar
 *  - Auto-eat when health or hunger is low
 *  - Awareness of item durability (switches if current weapon is about to break)
 *
 * Scaffolded / partially implemented (documented):
 *  - Armor awareness (reads armor slots, does not auto-equip yet)
 *  - Shield use (detects shield, does not auto-block yet – requires more input timing)
 *  - Potion / healing item management (detects, does not auto-consume yet)
 */
public class InventoryManager {
    private final ModConfig config;
    private int lastWeaponSlot = -1;
    private int eatCooldownTicks = 0;

    public InventoryManager(ModConfig config) {
        this.config = config;
    }

    public void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (config.autoSelectWeapon) {
            selectBestMeleeWeapon(player);
        }

        if (config.autoEat) {
            tryEat(client, player);
        }

        // Armor awareness (read-only for now)
        // Future: compare equipped vs inventory armor and swap if better.
        // player.getInventory().armor ... already accessible.

        // Shield awareness
        // ItemStack offhand = player.getOffhandItem();
        // if (offhand.is(Items.SHIELD)) { ... }

        if (eatCooldownTicks > 0) {
            eatCooldownTicks--;
        }
    }

    /**
     * Select the highest-scoring melee weapon currently in the hotbar (slots 0–8).
     * Prefers swords and axes; falls back to any high-damage item.
     */
    private void selectBestMeleeWeapon(LocalPlayer player) {
        Inventory inv = player.getInventory();
        int bestSlot = -1;
        float bestScore = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            // Skip nearly-broken items
            if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 2) {
                continue;
            }

            float score = scoreWeapon(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0 && bestSlot != inv.getSelectedSlot()) {
            inv.setSelectedSlot(bestSlot);
            lastWeaponSlot = bestSlot;
        }
    }

    /**
     * Rough damage / preference score for melee weapons.
     * Uses item type heuristics; attribute modifiers are consulted when present.
     */
    private float scoreWeapon(ItemStack stack) {
        float base = 0f;

        if (stack.getItem() instanceof SwordItem) {
            base = 6.0f;
            // Prefer higher-tier materials via max damage as a crude proxy
            base += Math.min(stack.getMaxDamage() / 500f, 4f);
        } else if (stack.getItem() instanceof AxeItem) {
            base = 7.0f; // axes often hit harder but slower
            base += Math.min(stack.getMaxDamage() / 500f, 3f);
        } else if (stack.is(Items.TRIDENT)) {
            base = 5.0f;
        } else {
            // Generic: look at attack damage attribute if available
            ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (mods != null) {
                // Simplified – real attribute lookup is more involved
                base = 1.0f;
            }
        }

        // Prefer undamaged
        if (stack.isDamageableItem()) {
            float durabilityRatio = 1.0f - ((float) stack.getDamageValue() / stack.getMaxDamage());
            base *= (0.7f + 0.3f * durabilityRatio);
        }

        return base;
    }

    /**
     * Eat food from the hotbar when health or hunger is below configured thresholds.
     * Uses the vanilla use item path so the server sees a normal interaction.
     */
    private void tryEat(Minecraft client, LocalPlayer player) {
        if (eatCooldownTicks > 0) return;
        if (player.isUsingItem()) return; // already eating / using something

        boolean needHealth = player.getHealth() < config.eatHealthThreshold;
        boolean needHunger = player.getFoodData().getFoodLevel() < config.eatHungerThreshold;
        if (!needHealth && !needHunger) return;

        Inventory inv = player.getInventory();
        int foodSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null && food.nutrition() > 0) {
                foodSlot = i;
                break;
            }
        }

        if (foodSlot < 0) return;

        // Switch to food, start using, then restore weapon later
        int previous = inv.getSelectedSlot();
        inv.setSelectedSlot(foodSlot);
        client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        // Simple cooldown so we don't spam
        eatCooldownTicks = 40;
        // Note: after eating finishes the player will still hold the food slot;
        // next weapon-selection tick will restore the best weapon.
    }

    // ---- Scaffolded helpers for future expansion ----

    /**
     * Returns true if the player is currently holding a shield in either hand.
     * Blocking logic is not yet automated (requires precise right-click timing
     * relative to incoming attacks).
     */
    public boolean isHoldingShield(LocalPlayer player) {
        return player.getMainHandItem().is(Items.SHIELD)
                || player.getOffhandItem().is(Items.SHIELD);
    }

    /**
     * Placeholder for future potion / golden-apple auto-use.
     * Currently only detects presence; does not consume.
     */
    public boolean hasHealingItem(LocalPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE)
                    || s.is(Items.POTION)) {
                return true;
            }
        }
        return false;
    }
}
