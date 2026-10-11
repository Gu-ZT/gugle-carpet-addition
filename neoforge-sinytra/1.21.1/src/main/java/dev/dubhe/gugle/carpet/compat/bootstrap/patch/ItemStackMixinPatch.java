package dev.dubhe.gugle.carpet.compat.bootstrap.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Minecraft 1.21.1 only; the payload is still in Fabric's intermediary namespace. */
public final class ItemStackMixinPatch {
    public static final String MIXIN = "dev/dubhe/gugle/carpet/mixin/ItemStackMixin";
    public static final String OLD_TARGET = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V";
    public static final String NEW_TARGET = OLD_TARGET.replace("net/minecraft/server/level/ServerPlayer", "net/minecraft/world/entity/LivingEntity");
    private static final String WRAP_OPERATION = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;";
    private static final String PLAYER = "Lnet/minecraft/class_3222;";
    private static final String ENTITY = "Lnet/minecraft/class_1309;";

    private ItemStackMixinPatch() {}

    public static byte[] rewriteClass(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        MethodNode method = node.methods.stream().filter(m -> m.name.equals("hurtAndBreak")).toList().getFirst();
        AnnotationNode wrap = method.visibleAnnotations.stream().filter(a -> a.desc.equals(WRAP_OPERATION)).toList().getFirst();
        @SuppressWarnings("unchecked")
        AnnotationNode at = ((List<AnnotationNode>) wrap.values.get(wrap.values.indexOf("at") + 1)).getFirst();
        at.values.set(at.values.indexOf("target") + 1, NEW_TARGET);

        Type[] arguments = Type.getArgumentTypes(method.desc);
        arguments[3] = Type.getType(ENTITY);
        method.desc = Type.getMethodDescriptor(Type.VOID_TYPE, arguments);
        method.signature = method.signature.replace(PLAYER, ENTITY);
        method.localVariables.stream()
            .filter(local -> local.index == 4 && PLAYER.equals(local.desc))
            .forEach(local -> local.desc = ENTITY);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static byte[] rewriteRefmap(byte[] bytes) {
        JsonObject root = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        boolean changed = rewriteMappings(root.getAsJsonObject("mappings"));
        JsonObject data = root.getAsJsonObject("data");
        if (data != null) {
            for (JsonElement namespace : data.asMap().values()) {
                changed |= rewriteMappings(namespace.getAsJsonObject());
            }
        }
        return changed ? root.toString().getBytes(StandardCharsets.UTF_8) : bytes;
    }

    private static boolean rewriteMappings(JsonObject mappings) {
        if (mappings == null) return false;
        JsonObject mixin = mappings.getAsJsonObject(MIXIN);
        if (mixin == null || !mixin.has(OLD_TARGET)) return false;
        mixin.addProperty(OLD_TARGET, NEW_TARGET);
        mixin.addProperty(NEW_TARGET, NEW_TARGET);
        return true;
    }
}
