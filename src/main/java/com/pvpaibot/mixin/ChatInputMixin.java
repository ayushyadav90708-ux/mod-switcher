package com.pvpaibot.mixin;

import com.pvpaibot.PvpAiClient;
import com.pvpaibot.command.PvpCommandHandler;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts outgoing chat messages so that #PVP commands never reach the server.
 * Hook is placed on the method that actually sends the chat packet.
 */
@Mixin(ClientPacketListener.class)
public class ChatInputMixin {

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void pvpai$onSendChat(String message, CallbackInfo ci) {
        PvpAiClient mod = PvpAiClient.getInstance();
        if (mod == null) return;

        PvpCommandHandler handler = mod.getCommandHandler();
        if (handler != null && handler.handleChat(message)) {
            ci.cancel();
        }
    }
}
