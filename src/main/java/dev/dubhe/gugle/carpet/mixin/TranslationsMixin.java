package dev.dubhe.gugle.carpet.mixin;

import carpet.CarpetSettings;
import carpet.utils.Translations;
import dev.dubhe.gugle.carpet.api.tools.text.ComponentTranslate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Translations.class, remap = false)
public class TranslationsMixin {
    @Inject(method = "updateLanguage", at = @At("HEAD"))
    private static void updateLanguage(CallbackInfo ci) {
        ComponentTranslate.updateLanguage(CarpetSettings.language);
    }
}
