package com.arcanewarrior.component;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Represents just a file to copy to the final pack
 */
public record CopyFileComponent(@NotNull Path source, @NotNull Path destination) { }
