package dev.dubhe.gugle.carpet.mixin;

import carpet.commands.PlayerCommand;
import dev.dubhe.gugle.carpet.GcaSetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Locale;

@Mixin(value = PlayerCommand.class, remap = false)
abstract class PlayerCommandMixin {
    @ModifyVariable(method = "spawn", at = @At("STORE"), remap = false)
    private static String spawn(String value) {
        return PlayerCommandMixin.gca$getName(value);
    }

    @ModifyVariable(method = "cantSpawn", at = @At("STORE"), remap = false)
    private static String cantSpawn(String value) {
        return PlayerCommandMixin.gca$getName(value);
    }

    @Unique
    private static String gca$getName(String value) {
        if (
            !GcaSetting.fakePlayerNoneName.equals(GcaSetting.fakePlayerPrefixName)
                && !value.toLowerCase(Locale.ROOT).startsWith(GcaSetting.fakePlayerPrefixName.toLowerCase(Locale.ROOT))
        ) {
            value = GcaSetting.fakePlayerPrefixName + value;
        }
        if (
            !GcaSetting.fakePlayerNoneName.equals(GcaSetting.fakePlayerSuffixName)
                && !value.toLowerCase(Locale.ROOT).endsWith(GcaSetting.fakePlayerSuffixName.toLowerCase(Locale.ROOT))
        ) {
            value = value + GcaSetting.fakePlayerSuffixName;
        }
        return value;
    }
}
