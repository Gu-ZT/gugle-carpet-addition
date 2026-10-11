package dev.dubhe.gugle.carpet.compat.bootstrap;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.locating.IDependencyLocator;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

/** Standalone sidecar, discovered by FML before Connector's dependency locator. */
public final class GcaDependencyLocator implements IDependencyLocator {
    private static final Logger log = LogUtils.getLogger();

    private boolean prepared;

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY; // Connector uses -1000.
    }

    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        if (prepared) return;
        try {
            Path original = WrapperPayload.findInput(FMLPaths.MODSDIR.get());
            WrapperPayload.Rewritten payload = WrapperPayload.rewrite(original);
            Path patched = FabricPayload.cache(
                payload.bytes(),
                FMLPaths.GAMEDIR.get().resolve(".cache/gca").resolve(payload.version())
            );
            // Prepare everything before publishing the replacement to discovery.
            LegacyDiscovery.exclude(original);
            FabricPayload.replaceModLocation(original, patched);
            prepared = true;
            log.info("Prepared {} for Connector as {}", original, patched);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to prepare installed GCA for Connector", e);
        }
    }
}
