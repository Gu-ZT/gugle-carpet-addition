package dev.dubhe.gugle.carpet.compat.bootstrap;

import cpw.mods.modlauncher.api.NamedPath;
import net.neoforged.fml.loading.ModDirTransformerDiscoverer;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Narrow, version-specific bridge for FML 4.x / Connector 2.x input exclusion. */
final class LegacyDiscovery {
    private LegacyDiscovery() {}

    static void exclude(Path original) {
        Path normalized = original.toAbsolutePath().normalize();
        excludeFromLegacyClasspath(normalized);
        // allExcluded() returns a new list, so modifying its result does nothing.
        // Connector consults that list when it subsequently scans mods/ and
        // additional locations. Add only the input we've already copied/patched.
        // No class transformation, on-disk rename, or static-final replacement.
        try {
            Field field = ModDirTransformerDiscoverer.class.getDeclaredField("found");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<NamedPath> found = (List<NamedPath>) field.get(null);
            synchronized (found) {
                if (found.stream().noneMatch(entry -> entry.paths()[0].equals(normalized))) {
                    found.add(new NamedPath("gca_sinytra_claimed_wrapper", normalized));
                }
            }
            if (!ModDirTransformerDiscoverer.allExcluded().contains(normalized)) {
                throw new IllegalStateException("FML did not exclude the original GCA input");
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException("Unsupported FML discovery implementation; cannot safely replace GCA's Connector input", e);
        }
    }

    private static void excludeFromLegacyClasspath(Path original) {
        String existing = System.getProperty("legacyClassPath", "");
        boolean present = Arrays.stream(existing.split(java.util.regex.Pattern.quote(File.pathSeparator)))
            .filter(path -> !path.isBlank())
            .map(path -> Path.of(path).toAbsolutePath().normalize())
            .anyMatch(original::equals);
        if (!present) {
            System.setProperty("legacyClassPath", existing.isBlank()
                ? original.toString()
                : existing + File.pathSeparator + original);
        }
    }
}
