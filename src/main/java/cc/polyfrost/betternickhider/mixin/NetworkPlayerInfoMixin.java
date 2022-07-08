package cc.polyfrost.betternickhider.mixin;

import cc.polyfrost.betternickhider.BetterNickHider;
import cc.polyfrost.betternickhider.config.BNHConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkPlayerInfo.class)
public class NetworkPlayerInfoMixin {
    @Shadow
    @Final
    private GameProfile gameProfile;

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Objects;firstNonNull(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    public void redirectLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if (BetterNickHider.config.enabled) {
            if (gameProfile.equals(Minecraft.getMinecraft().thePlayer.getGameProfile()) && BNHConfig.showSelfNormally)
                return;
            if (BNHConfig.useCustomSkinPath && BetterNickHider.config.customSkinLocation != null) {
                try {
                    cir.setReturnValue(BetterNickHider.config.customSkinLocation);
                } catch (Exception ignored) {
                }
            }
            if (BNHConfig.ownSkin && !gameProfile.equals(Minecraft.getMinecraft().thePlayer.getGameProfile())) {
                cir.setReturnValue(Minecraft.getMinecraft().thePlayer.getLocationSkin());
            } else if (BNHConfig.defaultSkin) {
                cir.setReturnValue(DefaultPlayerSkin.getDefaultSkin(gameProfile.getId()));
            }
        }
    }

    @Inject(method = "getSkinType", at = @At("TAIL"), cancellable = true)
    public void redirectSkinType(CallbackInfoReturnable<String> cir) {
        if (BetterNickHider.config.enabled) {
            if (gameProfile.equals(Minecraft.getMinecraft().thePlayer.getGameProfile()) && BNHConfig.showSelfNormally)
                return;
            if (BNHConfig.ownSkin && !gameProfile.equals(Minecraft.getMinecraft().thePlayer.getGameProfile())) {
                cir.setReturnValue(DefaultPlayerSkin.getSkinType(Minecraft.getMinecraft().thePlayer.getGameProfile().getId()));
            } else if (BNHConfig.defaultSkin) {
                cir.setReturnValue(DefaultPlayerSkin.getSkinType(gameProfile.getId()));
            }
        }
    }
}
