package dev.dubhe.gugle.carpet.compat;

import org.objectweb.asm.Type;
import org.sinytra.connector.transformer.api.TransformerContext;
import org.sinytra.connector.transformer.api.TransformerIds;
import org.sinytra.connector.transformer.api.TransformerPlugin;
import org.sinytra.connector.transformer.api.TransformerRegistrar;
import org.sinytra.connector.transformer.patch.ClassNodeTransformer;

import java.util.Set;

public class GCASinytraCompat implements TransformerPlugin {
    private static final String MOD_ID = "gca";

//    private static final String INJECT_DESC = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String WRAP_OPERATION_DESC = "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;";
    private static final String NEOFORGE_HURT_AND_BREAK_TARGET = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V";

    @Override
    public String name() {
        return MOD_ID + ":compat_plugin";
    }

    @Override
    public void registerJarTransformers(TransformerRegistrar registrar, TransformerContext context) {
        if (!MOD_ID.equals(context.candidateJar().modMetadata().getId())) return;

        registrar.registerBefore(
            MOD_ID + ":rewrite_itemstack_mixin",
            Set.of(TransformerIds.METHOD_PATCHES),
            new ClassNodeTransformer(new ClassResolver(
                "dev.dubhe.gugle.carpet.mixin.ItemStackMixin",
                "hurtAndBreak",
                WRAP_OPERATION_DESC,
                method -> {
                    Type[] arguments = Type.getArgumentTypes(method.desc);
                    arguments[3] = Type.getObjectType("net/minecraft/world/entity/LivingEntity");
                    method.desc = Type.getMethodDescriptor(Type.getReturnType(method.desc), arguments);
                },
                annotation ->
                    ClassResolver.setAnnotationValue(annotation, "target", NEOFORGE_HURT_AND_BREAK_TARGET)
            ))
        );
//        registrar.registerBefore(
//            MOD_ID + ":rewrite_main_server_mixin",
//            Set.of(TransformerIds.METHOD_PATCHES),
//            new ClassNodeTransformer(new ClassResolver(
//                "dev.dubhe.gugle.carpet.mixin.MainServerMixin",
//                "updateConfig",
//                INJECT_DESC,
//                _ -> {},
//                annotation -> ClassResolver.setAnnotationValue(annotation, "ordinal", 1)
//            ))
//        );

    }
}
