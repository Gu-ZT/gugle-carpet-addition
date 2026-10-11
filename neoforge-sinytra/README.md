# NeoForge / Sinytra Connector 兼容附件

`neoforge-sinytra` 包含 GCA 在 NeoForge / Sinytra Connector 环境中使用的版本专用兼容附件。目前提供 Minecraft 1.21.1 和 26.1.2 两个模块。

ModWrapper 不内嵌这些模块。发布时以 Wrapper 为主文件，两个兼容 JAR 作为附加文件上传（Modrinth additional files / CurseForge child files）。NeoForge 用户只安装普通 Wrapper 和与 Minecraft 版本匹配的一个兼容附件，不要同时安装两个附件。Fabric/Quilt 不需要兼容附件。

## 构建

构建 Wrapper：

```powershell
.\gradlew :ModWrapper:build --no-daemon '-Dorg.gradle.java.home=C:\Program Files\Zulu\zulu-25'
```

单独构建两个兼容附件：

```powershell
.\gradlew :neoforge-sinytra-mc1.21.1:jar :neoforge-sinytra-mc26.1.2:jar --no-daemon '-Dorg.gradle.java.home=C:\Program Files\Zulu\zulu-25'
```

Wrapper 产物位于 `ModWrapper/build/libs/`。兼容附件分别位于 `neoforge-sinytra/1.21.1/build/libs/` 和 `neoforge-sinytra/26.1.2/build/libs/`，不会复制到 Wrapper 输出目录。`:ModWrapper:publishMods` 通过产物 Provider 直接引用并上传两个附件。

运行时仍需安装对应版本的 Connector、Forgified Fabric API 和 Carpet。

## Minecraft 1.21.1

旧版 Connector 没有 TransformerPlugin 注册接口。1.21.1 附件通过 `IDependencyLocator` 加载，优先级 0，先于 Connector 的 -1000 扫描执行。

附件从 `mods/` 和 `connector.additionalModLocations` 查找唯一的 GCA Wrapper，也支持调试用的原始 GCA 1.21.1 JAR。它读取 Wrapper 中的 GCA 1.21.1 与 Loom 标记的共享库，生成只包含这一版本及共享库的 Fabric Wrapper 副本，再交给 Connector 转换。

原 Wrapper 会加入 FML 的发现排除列表和 Connector 的旧 classpath 排除集合，避免原始内容被重复转换。额外位置中的原文件别名也会被移除。FML 发现排除依赖私有的 `ModDirTransformerDiscoverer.found`，升级 FML 或 Connector 后需要重新验证。

缓存位于游戏目录 `.cache/gca/{version}/`，其中 `version` 来自所选 GCA 的 `fabric.mod.json`，例如 `2.12.9-mc1.21.1`。缓存 JAR 使用输出 SHA-256 命名，损坏时自动重建，不会修改安装的 Wrapper。

在 Connector `METHOD_PATCHES` 之前执行的补丁包括：

- 修改 `ItemStackMixin.hurtAndBreak` 的 handler、`@At.target` 和 refmap，将 `ServerPlayer` 参数替换为 `LivingEntity`。
- 修改 `BotSpawnUtil.spawnBot` 对 `EntityPlayerMPFake.respawnFake` 的调用，移除第一个 `MinecraftServer` 实参。
- 为 `MainServerMixin.updateConfig` 的唯一 `@At` 添加 `ordinal = 1`。

## Minecraft 26.1.2

26.1.2 附件使用 Connector 3.x 提供的 `TransformerPlugin`，通过 `registerBefore(..., TransformerIds.METHOD_PATCHES, ...)` 注册补丁。当前补丁修改 `ItemStackMixin.hurtAndBreak` 的方法参数及 `@At.target`，由 Connector 在后续阶段继续完成映射和转换。

构建和字节码组件检查不能替代实际游戏启动验证。升级加载器、Connector 或 Carpet 后，应重新进行完整启动测试。
