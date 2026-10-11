package dev.dubhe.gugle.carpet.neoforgedebug;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A deliberately tiny NeoForge-only probe.  It gives a stable place for
 * Connector compatibility hooks and class-loader diagnostics while the real
 * Fabric artifact remains untouched.
 */
@Mod(GcaNeoForgeDebug.MOD_ID)
public final class GcaNeoForgeDebug {
    public static final String MOD_ID = "gca_neoforge_debug";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public GcaNeoForgeDebug(ModContainer container) {
        LOGGER.info("GCA NeoForge debug module loaded (NeoForge={}, Connector={})",
                System.getProperty("neoforge.version", "unknown"),
                isConnectorPresent() ? "present" : "missing");
    }

    private static boolean isConnectorPresent() {
        try {
            Class.forName("org.sinytra.connector.Connector", false,
                    GcaNeoForgeDebug.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
