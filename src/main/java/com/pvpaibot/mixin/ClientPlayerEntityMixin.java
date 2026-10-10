package com.pvpaibot.mixin;

import com.pvpaibot.PvpAiClient;
import com.pvpaibot.combat.CombatController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures movement keys are released if the local player is removed
 * (death, dimension change, etc.) while the bot is active.
 */
@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "remove" /* or equivalent removal hook */, at = @At("HEAD"), require = 0)
    private void pvpai$onRemove(CallbackInfo ci) {
        // Soft hook – method name may vary; combat controller also checks every tick.
        PvpAiClient mod = PvpAiClient.getInstance();
        if (mod != null) {
            CombatController cc = mod.getCombatController();
            if (cc != null && cc.isActive()) {
                cc.stop("Local player removed");
            }
        }
    }
}
