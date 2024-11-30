package com.arcanewarrior;

import com.arcanewarrior.component.JsonPackComponent;
import com.arcanewarrior.component.McMetaComponent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    }

    private void writeJsonToFile(@NotNull Gson gson, @NotNull JsonPackComponent component) throws IOException {
        try (FileWriter fileWriter = new FileWriter(component.filePath().toFile())) {
            gson.toJson(component.buildComponent(), fileWriter);
            fileWriter.flush();
        }
    }
}
