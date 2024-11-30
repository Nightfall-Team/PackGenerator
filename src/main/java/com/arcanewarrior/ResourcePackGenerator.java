package com.arcanewarrior;

import com.arcanewarrior.component.CopyFileComponent;
import com.arcanewarrior.component.JsonPackComponent;
import com.arcanewarrior.component.McMetaComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        if (!Files.exists(workingDirectory)) {
            Files.createDirectory(workingDirectory);
        }
        // Generate pack.mcmeta file
        JsonPackComponent mcMetaComponent = new McMetaComponent();
        writeJsonToFile(gson, mcMetaComponent);
        // Copy icon
        CopyFileComponent component = new CopyFileComponent(Path.of("pack.png"), workingDirectory.resolve("pack.png"));
        Files.copy(component.source(), component.destination(), StandardCopyOption.REPLACE_EXISTING);

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

    private void writeJsonToFile(@NotNull Gson gson, @NotNull JsonPackComponent component) throws IOException {
        try (FileWriter fileWriter = new FileWriter(component.filePath().toFile())) {
            gson.toJson(component.buildComponent(), fileWriter);
            fileWriter.flush();
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
            System.out.println(hexString);
            Files.writeString(ResourcePackConstants.SHA1_FILE, hexString, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Failed to find SHA1 algorithm!");
        }
    }
}
