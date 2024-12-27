package com.arcanewarrior;

import com.arcanewarrior.component.*;
import com.arcanewarrior.misc.UnicodeWorkaroundWriter;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackGenerator {

    private final String packName;
    private final String namespace;

    public ResourcePackGenerator(@NotNull String packName, @NotNull String namespace) {
        this.packName = packName;
        this.namespace = namespace;
    }

    public void buildPack() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Create working directory
        Path workingDirectory = ResourcePackConstants.WORKING_DIRECTORY;
        Path namespacedPath = workingDirectory.resolve("assets").resolve(namespace);
        Path namespacedSource = ResourcePackConstants.PACK_ASSETS_FOLDER.resolve("assets").resolve(namespace);

        // Create our working directory
        if (!Files.exists(workingDirectory)) {
            Files.createDirectory(workingDirectory);
        }
        // TODO: the working directory doesn't delete its contents, should we add a task here to do so?

        // Generate our json files
        generateJsonComponents(gson, namespacedPath);
        // Copy static assets from our pack assets folder
        copyExistingFiles(namespacedSource, namespacedPath);

        String outputFileName = packName.endsWith(".zip") ? packName : packName + ".zip";
        Path output = Path.of(outputFileName);
        if (Files.exists(output)) {
            // Delete it if it exists
            Files.delete(output);
        }
        Files.createFile(output);
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(output))) {
            try (Stream<Path> inputFiles = Files.walk(workingDirectory)) {
                inputFiles
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                ZipEntry entry = new ZipEntry(workingDirectory.relativize(path).toString());
                                outputStream.putNextEntry(entry);
                                Files.copy(path, outputStream);
                                outputStream.closeEntry();
                            } catch (IOException e) {
                                System.err.println("Failed to zip files, " + e.getMessage());
                            }
                        });
            }
        }
        // Generate SHA1
        generateSha1(output);
    }

    private void generateJsonComponents(@NotNull Gson gson, @NotNull Path namespacedPath) throws IOException {
        List<JsonPackComponent> jsonComponents = List.of(
                new McMetaComponent(), // Pack McMeta file
                new NegativeSpaceFontComponent(namespacedPath), // Negative Space font
                new CustomHealthbarComponent(namespacedPath), // Custom healthbar icons
                new MinecraftFontOverrideComponent() // Add our font providers to minecraft's default font
        );
        for (JsonPackComponent jsonPackComponent : jsonComponents) {
            Path filePath = jsonPackComponent.filePath();
            // Ensure all directories exist
            Files.createDirectories(filePath.getParent());
            if (jsonPackComponent.needsToWriteUnicodeChar()) {
                // Workaround for Gson issue, see UnicodeWorkaroundWriter
                try (UnicodeWorkaroundWriter writer = new UnicodeWorkaroundWriter(filePath)) {
                    JsonWriter jsonWriter = new JsonWriter(writer);
                    jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
                    gson.toJson(jsonPackComponent.buildComponent(), jsonWriter);
                    writer.flush();
                }
            } else {
                try (FileWriter fileWriter = new FileWriter(filePath.toFile())) {
                    gson.toJson(jsonPackComponent.buildComponent(), fileWriter);
                    fileWriter.flush();
                }
            }
        }
    }

    private void copyExistingFiles(@NotNull Path namespaceSource, @NotNull Path namespacedPath) throws IOException {
        // Copy icon
        List<CopyFileComponent> copyFileComponents = List.of(
                new CopyFileComponent(ResourcePackConstants.PACK_ASSETS_FOLDER.resolve("pack.png"), ResourcePackConstants.WORKING_DIRECTORY.resolve("pack.png"))
        );
        for (CopyFileComponent component : copyFileComponents) {
            if (Files.exists(component.source())) {
                Files.createDirectories(component.destination().getParent());
                Files.copy(component.source(), component.destination(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        // Copy file structure from the assets 'namespace' folder
        try (var files = Files.walk(namespaceSource)) {
            files.forEach(source -> {
                try {
                    Path destinationPath = namespacedPath.resolve(namespaceSource.relativize(source));
                    if (Files.isDirectory(source)) {
                        if (!Files.exists(destinationPath))
                            Files.createDirectory(destinationPath);
                    } else {
                        Files.copy(source, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy existing assets over.");
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to copy over pack assets. " + e.getMessage());
        }
    }

    private void generateSha1(@NotNull Path zippedPackPath) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(Files.readAllBytes(zippedPackPath));
            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            Files.writeString(ResourcePackConstants.SHA1_FILE, hexString, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Failed to find SHA1 algorithm!");
        }
    }
}
