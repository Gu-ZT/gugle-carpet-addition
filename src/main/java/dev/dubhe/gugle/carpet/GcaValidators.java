package dev.dubhe.gugle.carpet;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import javax.annotation.Nullable;

public class GcaValidators {
    public static final boolean CARPET_AMS_ADDITION = FabricLoader.getInstance().isModLoaded("carpet-ams-addition");

    public static class EnderChest extends Validator<String> {
        public static final List<String> OPTIONS = List.of("true", "false", "ender_chest");

        @Override
        public @Nullable String validate(@Nullable CommandSourceStack commandSourceStack, CarpetRule<String> carpetRule, String newValue, String userString) {
            return !OPTIONS.contains(newValue) ? null : newValue;
        }

        public String description() {
            return "Can be limited to 'ender_chest' for use EnderChest open only, true/false for open directly/unable";
        }
    }

    public static class CommandLevelWithVanilla extends Validator<String> {
        public static final List<String> OPTIONS = List.of("vanilla", "true", "false", "ops", "0", "1", "2", "3", "4");

        @Override
        public @Nullable String validate(@Nullable CommandSourceStack commandSourceStack, CarpetRule<String> carpetRule, String newValue, String userString) {
            return OPTIONS.contains(newValue) ? newValue : null;
        }

        public String description() {
            return "Can be limited to 'vanilla' or command level options";
        }
    }

    public static class CarpetAmsAdditionLoaded implements Rule.Condition {
        @Override
        public boolean shouldRegister() {
            return !CARPET_AMS_ADDITION;
        }
    }

    public static class PositiveNumber extends Validator<Integer> {
        @Override
        public @Nullable Integer validate(@Nullable CommandSourceStack source, CarpetRule<Integer> currentRule, Integer newValue, String string) {
            return newValue.doubleValue() > 0 ? newValue : null;
        }
        @Override
        public String description() { return "Must be a positive number";}
    }
}
