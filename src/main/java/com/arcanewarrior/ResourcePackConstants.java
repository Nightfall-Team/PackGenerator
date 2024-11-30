package com.arcanewarrior;

import java.nio.file.Path;

public class ResourcePackConstants {
    public static final String PACK_DESCRIPTION = "The pack for the Nightfall Server (that was written in Minestom).";
    public static final int PACK_FORMAT = 34;
    public static final int MAX_SUPPORTED_FORMAT = 46;

    public static final Path PACK_ASSETS_FOLDER = Path.of("pack-assets");
    public static final Path WORKING_DIRECTORY = Path.of("resource-pack-build");
    public static final Path ASSETS = WORKING_DIRECTORY.resolve("assets");

    public static final Path SHA1_FILE = Path.of("sha1.txt");
}
