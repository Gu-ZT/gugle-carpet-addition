package dev.dubhe.gugle.carpet.compat.bootstrap.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public final class MainServerMixinPatch {
    public static final String MIXIN = "dev/dubhe/gugle/carpet/mixin/MainServerMixin";
    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";

    private MainServerMixinPatch() {}

    public static byte[] rewriteClass(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        var method = node.methods.stream().filter(candidate -> candidate.name.equals("updateConfig")).toList().getFirst();
        var inject = method.visibleAnnotations.stream().filter(annotation -> annotation.desc.equals(INJECT)).toList().getFirst();
        @SuppressWarnings("unchecked")
        AnnotationNode at = ((List<AnnotationNode>) inject.values.get(inject.values.indexOf("at") + 1)).getFirst();
        at.values.add("ordinal");
        at.values.add(1);

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
