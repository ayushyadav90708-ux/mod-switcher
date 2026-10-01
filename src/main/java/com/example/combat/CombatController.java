package com.example.combat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;

public class CombatController {
    private static int comboStep = 0;
    private static long lastAttackTime = 0;
    private static final long COMBO_RESET_WINDOW = 650; // Milliseconds before combo resets

    public static void handlePlayerAttack(PlayerEntity player, ItemStack weapon) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAttackTime > COMBO_RESET_WINDOW) {
            comboStep = 0;
        }

        if (weapon.getItem() instanceof SwordItem) {
            executeSwordCombo(player, comboStep);
        } else if (weapon.getItem() instanceof AxeItem) {
            executeAxeHeavyStrike(player);
        } else {
            executeDefaultStrike(player, comboStep);
        }

        comboStep = (comboStep + 1) % 3;
        lastAttackTime = currentTime;
    }

    private static void executeSwordCombo(PlayerEntity player, int step) {
        switch (step) {
            case 0:
                triggerClientAnimation(player, "sword_jab");
                break;
            case 1:
                triggerClientAnimation(player, "sword_slash");
                break;
            case 2:
                triggerClientAnimation(player, "sword_finisher");
                break;
        }
    }

    private static void executeAxeHeavyStrike(PlayerEntity player) {
        triggerClientAnimation(player, "axe_heavy_slam");
    }

    private static void executeDefaultStrike(PlayerEntity player, int step) {
        triggerClientAnimation(player, "generic_strike_" + step);
    }

    private static void triggerClientAnimation(PlayerEntity player, String animationName) {
        // Client-side animation trigger logic placeholder
    }
}
