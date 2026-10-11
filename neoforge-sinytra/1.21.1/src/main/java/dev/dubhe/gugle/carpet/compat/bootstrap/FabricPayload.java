package dev.dubhe.gugle.carpet.compat.bootstrap;

import dev.dubhe.gugle.carpet.compat.bootstrap.patch.BotSpawnUtilPatch;
import dev.dubhe.gugle.carpet.compat.bootstrap.patch.ItemStackMixinPatch;
import dev.dubhe.gugle.carpet.compat.bootstrap.patch.MainServerMixinPatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class FabricPayload {
    private static final String ADDITIONAL_MODS = "connector.additionalModLocations";

    private FabricPayload() {}

    static Path cache(byte[] patched, Path cache) throws IOException {
        // Hash the output, not just the source: a patch change must invalidate
        // Connector's cache too. ZIP timestamps are fixed for reproducibility.
        String hash;
        try {
            hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(patched));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        Files.createDirectories(cache);
        Path output = cache.resolve("gca-" + hash + ".jar").toAbsolutePath().normalize();
        if (Files.isRegularFile(output) && Arrays.equals(Files.readAllBytes(output), patched)) return output;

        Path temporary = Files.createTempFile(cache, "gca-", ".tmp");
        try {
            Files.write(temporary, patched);
            try {
                Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return output;
    }

    static byte[] rewrite(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean mixinFound = false;
        boolean refmapFound = false;
        try (ZipInputStream source = new ZipInputStream(input);
             ZipOutputStream output = new ZipOutputStream(bytes)) {
            ZipEntry entry;
            while ((entry = source.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) continue;
                String upper = name.toUpperCase(Locale.ROOT);
                if (upper.startsWith("META-INF/") && (upper.endsWith(".SF") || upper.endsWith(".RSA")
                    || upper.endsWith(".DSA") || upper.startsWith("META-INF/SIG-"))) continue;
                byte[] content = source.readAllBytes();
                if (name.equals(ItemStackMixinPatch.MIXIN + ".class")) {
                    content = ItemStackMixinPatch.rewriteClass(content);
                    mixinFound = true;
                } else if (name.equals(MainServerMixinPatch.MIXIN + ".class")) {
                    content = MainServerMixinPatch.rewriteClass(content);
                } else if (name.equals(BotSpawnUtilPatch.CLASS + ".class")) {
                    content = BotSpawnUtilPatch.rewriteClass(content);
                } else if (name.endsWith("-refmap.json")) {
                    byte[] rewritten = ItemStackMixinPatch.rewriteRefmap(content);
                    refmapFound |= !Arrays.equals(rewritten, content);
                    content = rewritten;
                }
                ZipEntry target = new ZipEntry(name);
                target.setTimeLocal(java.time.LocalDateTime.of(1980, 1, 1, 0, 0));
                output.putNextEntry(target);
                output.write(content);
                output.closeEntry();
            }
        }
        if (!mixinFound || !refmapFound) {
            throw new IOException("Unsupported GCA payload: ItemStackMixin or its original refmap entry is missing");
        }
        return bytes.toByteArray();
    }

    static synchronized void replaceModLocation(Path original, Path jar) {
        String location = jar.toAbsolutePath().normalize().toString();
        if (location.contains(",")) {
            throw new IllegalArgumentException("Connector cannot accept a mod path containing commas: " + location);
        }
        // Explicit additional input paths may use a relative spelling, unlike
        // Connector's absolute mods-dir scan. Remove those aliases too; directory
        // inputs are covered by FML's exclusion list.
        String existing = String.join(",", Arrays.stream(System.getProperty(ADDITIONAL_MODS, "").split(","))
            .filter(s -> !s.isBlank())
            .map(s -> Path.of(s).toAbsolutePath().normalize().toString())
            .filter(s -> !Path.of(s).toAbsolutePath().normalize().equals(original.toAbsolutePath().normalize()))
            .toList());
        boolean present = Arrays.stream(existing.split(",")).filter(s -> !s.isBlank())
            .anyMatch(s -> Path.of(s).toAbsolutePath().normalize().equals(jar.toAbsolutePath().normalize()));
        System.setProperty(ADDITIONAL_MODS, present ? existing : existing.isBlank() ? location : existing + "," + location);
    }
}
