package com.arcanewarrior.component;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Represents a resource pack component that uses JSON and creates a file in the pack
 */
public interface JsonPackComponent {

    @NotNull
    JsonElement buildComponent();

    @NotNull
    Path filePath();

    boolean needsToWriteUnicodeChar();
}
