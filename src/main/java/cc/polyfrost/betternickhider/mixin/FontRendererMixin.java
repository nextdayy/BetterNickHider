package cc.polyfrost.betternickhider.mixin;

import cc.polyfrost.betternickhider.BetterNickHider;
import cc.polyfrost.betternickhider.config.BNHConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public class FontRendererMixin {
    @ModifyVariable(method = "getStringWidth", at = @At(value = "HEAD"), argsOnly = true)
    public String resetWidth(String input) {
        return replaceNameInstance(input);
    }

    @ModifyVariable(method = "renderString", at = @At("HEAD"), argsOnly = true)
    public String removeNames(String input) {
        return replaceNameInstance(input);
    }

    public String replaceNameInstance(String input) {
        if(BetterNickHider.config == null) return input;
        if(BetterNickHider.config.enabled) {
            if (BetterNickHider.INSTANCE.nickCache == null) return input;
            if (input.contains(Minecraft.getMinecraft().getSession().getProfile().getName())) {
                if (BNHConfig.useCustomUsername) {
                    return input.replace(Minecraft.getMinecraft().getSession().getProfile().getName(), BNHConfig.customUsername);
                } else if (BNHConfig.showSelfNormally) return input;
            }
            if (BNHConfig.useRandomUsernames) {
                for (String s : BetterNickHider.INSTANCE.nickCache.asMap().keySet()) {
                    if (input.contains(s)) {
                        input = input.replace(s, BetterNickHider.INSTANCE.getPlayerName(s));
                    }
                }
            }
        }
        return input;
    }
}
