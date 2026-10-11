package dev.dubhe.gugle.carpet.compat;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.sinytra.adapter.env.ctx.PatchResult;
import org.sinytra.connector.transformer.patch.ClassNodeTransformer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClassResolver implements ClassNodeTransformer.ClassProcessor {
    private final String className;
    private final String methodName;
    private final String annotationName;
    @Nullable
    private final Consumer<MethodNode> methodProcessor;
    @Nullable
    private final Consumer<AnnotationNode> annotationProcessor;

    public ClassResolver(
        String className, String methodName, String annotationName,
        @Nullable Consumer<MethodNode> methodProcessor,
        @Nullable Consumer<AnnotationNode> annotationProcessor
    ) {
        this.className = className;
        this.methodName = methodName;
        this.annotationName = annotationName;
        this.methodProcessor = methodProcessor;
        this.annotationProcessor = annotationProcessor;
    }

    @Override
    public PatchResult process(ClassNode node) {
        if (!this.className.equals(node.name)) return PatchResult.PASS;

        MethodNode method = node.methods.stream()
            .filter(m -> this.methodName.equals(m.name))
            .findFirst()
            .orElse(null);

        if (method == null) {
            return PatchResult.PASS;
        }

        if (this.methodProcessor != null) {
            this.methodProcessor.accept(method);
        }

        if (this.annotationProcessor != null) {
            AnnotationNode wrapOperation = findAnnotation(method, this.annotationName);
            AnnotationNode at = (AnnotationNode) getAnnotationValue(wrapOperation, "at");
            if (at != null) {
                this.annotationProcessor.accept(at);
            }
        }

        return PatchResult.COMPUTE_FRAMES;
    }

    public static AnnotationNode findAnnotation(MethodNode method, String descriptor) {
        AnnotationNode annotation = findAnnotation(method.visibleAnnotations, descriptor);
        if (annotation != null) {
            return annotation;
        }
        return findAnnotation(method.invisibleAnnotations, descriptor);
    }

    public static AnnotationNode findAnnotation(List<AnnotationNode> annotations, String descriptor) {
        if (annotations == null) {
            return null;
        }

        for (AnnotationNode annotation : annotations) {
            if (descriptor.equals(annotation.desc)) {
                return annotation;
            }
        }
        return null;
    }

    public static Object getAnnotationValue(AnnotationNode annotation, String name) {
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (name.equals(annotation.values.get(i))) {
                return annotation.values.get(i + 1);
            }
        }

        return null;
    }

    public static void setAnnotationValue(AnnotationNode annotation, String name, Object value) {
        if (annotation.values == null) {
            annotation.values = new ArrayList<>();
        }

        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (annotation.values.get(i).equals(name)) {
                annotation.values.set(i + 1, value);
                return;
            }
        }

        annotation.values.add(name);
        annotation.values.add(value);
    }
}
