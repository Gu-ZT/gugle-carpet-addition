package dev.dubhe.gugle.carpet.compat.bootstrap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Finds the independently installed wrapper and prepares a Fabric-only copy. */
final class WrapperPayload {
    record Rewritten(byte[] bytes, String version) {}

    private WrapperPayload() {}

    static Path findInput(Path mods) throws IOException {
        var locations = new LinkedHashSet<Path>();
        locations.add(mods.toAbsolutePath().normalize());
        for (String value : System.getProperty("connector.additionalModLocations", "").split(",")) {
            if (!value.isBlank()) locations.add(Path.of(value).toAbsolutePath().normalize());
        }
        var candidates = new LinkedHashSet<Path>();
        for (Path location : locations) {
            if (Files.isDirectory(location)) {
                try (var files = Files.list(location)) {
                    for (Path file : files.filter(Files::isRegularFile).toList()) inspect(file, candidates);
                }
            } else if (Files.isRegularFile(location)) inspect(location, candidates);
        }
        if (candidates.size() != 1) throw new IOException("Expected exactly one installed GCA Wrapper or GCA 1.21.1 jar; found " + candidates);
        return candidates.iterator().next();
    }

    private static void inspect(Path file, LinkedHashSet<Path> candidates) throws IOException {
        if (!file.toString().endsWith(".jar")) return;
        try (ZipFile zip = new ZipFile(file.toFile())) {
            if (zip.getEntry("fabric.mod.json") == null) return;
            JsonObject metadata = json(read(zip, "fabric.mod.json"));
            String id = metadata.get("id").getAsString();
            if (id.equals("gca_wrapper") || id.equals("gca") && is1211(metadata)) candidates.add(file);
        }
    }

    private static boolean is1211(JsonObject metadata) {
        return metadata.get("version").getAsString().matches(".*-mc1\\.21\\.1(?:\\+.*)?");
    }

    static Rewritten rewrite(Path original) throws IOException {
        try (ZipFile source = new ZipFile(original.toFile())) {
            JsonObject metadata = json(read(source, "fabric.mod.json"));
            if (metadata.get("id").getAsString().equals("gca")) {
                if (!is1211(metadata)) throw new IOException("Expected GCA 1.21.1");
                return new Rewritten(
                    FabricPayload.rewrite(Files.newInputStream(original)),
                    metadata.get("version").getAsString()
                );
            }
            var nested = new LinkedHashMap<String, byte[]>();
            int selected = 0;
            String version = null;
            for (var jar : metadata.getAsJsonArray("jars")) {
                String path = jar.getAsJsonObject().get("file").getAsString();
                byte[] content = read(source, path);
                JsonObject child = null;
                try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        if (entry.getName().equals("fabric.mod.json")) { child = json(zip.readAllBytes()); break; }
                    }
                }
                if (child == null) throw new IOException("Missing nested Fabric metadata: " + path);
                if (child.get("id").getAsString().equals("gca")) {
                    if (!is1211(child)) continue;
                    selected++;
                    version = child.get("version").getAsString();
                    content = FabricPayload.rewrite(new ByteArrayInputStream(content));
                } else if (!child.has("custom") || !child.getAsJsonObject("custom").has("fabric-loom:generated")
                    || !child.getAsJsonObject("custom").get("fabric-loom:generated").getAsBoolean()) continue;
                if (nested.putIfAbsent(path, content) != null) throw new IOException("Duplicate nested jar: " + path);
            }
            if (selected != 1) throw new IOException("Expected exactly one GCA 1.21.1 payload; found " + selected);

            // The installed wrapper is excluded from Connector discovery and
            // excluded from Connector's scan. Supply a Fabric-only copy containing
            // just 1.21.1 and shared libraries, never competing GCA versions or the
            // Connector 3.x plugin. Fabric itself still sees the original full wrapper.
            JsonArray jars = new JsonArray();
            for (String path : nested.keySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("file", path);
                jars.add(entry);
            }
            metadata.add("jars", jars);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                write(zip, "fabric.mod.json", metadata.toString().getBytes(StandardCharsets.UTF_8));
                if (metadata.has("icon") && metadata.get("icon").isJsonPrimitive()) {
                    String icon = metadata.get("icon").getAsString();
                    write(zip, icon, read(source, icon));
                }
                for (var entry : nested.entrySet()) write(zip, entry.getKey(), entry.getValue());
            }
            return new Rewritten(bytes.toByteArray(), version);
        }
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) throw new IOException("Missing ModWrapper resource: " + path);
        try (InputStream input = zip.getInputStream(entry)) {
            return input.readAllBytes();
        }
    }

    private static JsonObject json(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void write(ZipOutputStream zip, String path, byte[] bytes) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        entry.setTimeLocal(LocalDateTime.of(1980, 1, 1, 0, 0));
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }
}
