package dev.dubhe.gugle.carpet.compat.bootstrap.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

public final class BotSpawnUtilPatch {
    public static final String CLASS = "dev/dubhe/gugle/carpet/util/BotSpawnUtil";
    private static final String FAKE_PLAYER = "carpet/patches/EntityPlayerMPFake";
    private static final String SPAWN_BOT_DESC = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/class_3218;Ldev/dubhe/gugle/carpet/entry/BotInfo;Lcom/mojang/authlib/GameProfile;ZLcarpet/helpers/EntityPlayerActionPack;)Z";
    private static final String NEW_DESC = "(Lnet/minecraft/class_3218;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/class_8791;)Lcarpet/patches/EntityPlayerMPFake;";

    private BotSpawnUtilPatch() {}

    public static byte[] rewriteClass(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        MethodNode method = node.methods.stream()
            .filter(candidate -> candidate.name.equals("spawnBot") && candidate.desc.equals(SPAWN_BOT_DESC))
            .toList().getFirst();
        MethodInsnNode call = Arrays.stream(method.instructions.toArray())
            .filter(MethodInsnNode.class::isInstance)
            .map(MethodInsnNode.class::cast)
            .filter(candidate -> candidate.owner.equals(FAKE_PLAYER) && candidate.name.equals("respawnFake"))
            .toList().getFirst();
        AbstractInsnNode server = call.getPrevious().getPrevious().getPrevious().getPrevious();
        method.instructions.remove(server);
        call.desc = NEW_DESC;

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
