package dev.dubhe.gugle.carpet.util;

import dev.dubhe.gugle.carpet.config.GcaConfig;
import dev.dubhe.gugle.carpet.config.IIdNode;

import java.util.Collection;

public class IdUtil {
    public static long nextId(GcaConfig<? extends IIdNode> config) {
        return nextId(config.values());
    }

    public static long nextId(Collection<? extends IIdNode> collection) {
        return collection.stream().mapToLong(IIdNode::id).max().orElse(0L) + 1;
    }
}
