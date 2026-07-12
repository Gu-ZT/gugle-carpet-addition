package dev.dubhe.gugle.carpet.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.nfunk.jep.JEP;

public class SimpleInGameCalculator {
    public static void handleChat(MinecraftServer server, String msg) {
        if (msg.startsWith("=")) return;
        server.getPlayerList().broadcastSystemMessage(SimpleInGameCalculator.calculate(msg), false);
    }

    public static Component calculate(String expression) {
        if (expression.startsWith("==")) expression = expression.substring(2);
        JEP jep = new JEP();
        // 添加常用函数
        jep.addStandardFunctions();
        // 添加常用常量
        jep.addStandardConstants();
        // 添加虚数
        jep.addComplex();
        jep.parseExpression(expression);
        if (!jep.hasError()) {
            double result = jep.getValue();
            return Component.literal("=%f".formatted(result)).withStyle(ChatFormatting.GRAY);
        } else {
            return Component.literal("Illegal expression: %s".formatted(jep.getErrorInfo()))
                .withStyle(ChatFormatting.RED);
        }
    }
}
