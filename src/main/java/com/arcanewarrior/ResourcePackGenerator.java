package com.arcanewarrior;

import com.arcanewarrior.component.CopyFileComponent;
import com.arcanewarrior.component.JsonPackComponent;
import com.arcanewarrior.component.McMetaComponent;
import com.arcanewarrior.component.NegativeSpaceFontComponent;
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
        Path namespacedPath = workingDirectory.resolve(namespace);

        // Create our working directory
        if (!Files.exists(workingDirectory)) {
            Files.createDirectory(workingDirectory);
        }

        // Generate our json files
        generateJsonComponents(gson, namespacedPath);
        // Copy static assets from our pack assets folder
        copyExistingFiles();

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
                new NegativeSpaceFontComponent(namespacedPath) // Negative Space font
        );
        for (JsonPackComponent jsonPackComponent : jsonComponents) {
            Path filePath = jsonPackComponent.filePath();
            // Ensure all directories exist
            Files.createDirectories(filePath.getParent());
            if (jsonPackComponent instanceof NegativeSpaceFontComponent) {
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

    private void copyExistingFiles() throws IOException {
        // Copy icon
        CopyFileComponent component = new CopyFileComponent(ResourcePackConstants.PACK_ASSETS_FOLDER.resolve("pack.png"), ResourcePackConstants.WORKING_DIRECTORY.resolve("pack.png"));
        Files.copy(component.source(), component.destination(), StandardCopyOption.REPLACE_EXISTING);
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
