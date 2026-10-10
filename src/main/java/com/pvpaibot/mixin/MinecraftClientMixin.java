package com.pvpaibot.mixin;

import com.pvpaibot.PvpAiClient;
import com.pvpaibot.combat.CombatController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Safety net: stop the bot when the Minecraft client is shutting down
 * or the world is unloaded.
 */
@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "disconnect" /* client disconnect path */, at = @At("HEAD"), require = 0)
    private void pvpai$onDisconnect(CallbackInfo ci) {
        PvpAiClient mod = PvpAiClient.getInstance();
        if (mod != null) {
            CombatController cc = mod.getCombatController();
            if (cc != null && cc.isActive()) {
                cc.stop("Client disconnect");
            }
        }
    }
}
