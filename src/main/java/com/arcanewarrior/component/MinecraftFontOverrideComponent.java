package com.arcanewarrior.component;

import com.arcanewarrior.ResourcePackConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class MinecraftFontOverrideComponent implements JsonPackComponent{

    @Override
    public @NotNull JsonElement buildComponent() {
        JsonObject root = new JsonObject();
        JsonArray providers = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "reference");
        jsonObject.addProperty("id", "nightfall:font/negative-spaces.json");
        providers.add(jsonObject);
        root.add("providers", providers);
        return root;
    }

    @Override
    public @NotNull Path filePath() {
        return ResourcePackConstants.WORKING_DIRECTORY.resolve("assets/minecraft/font/default.json");
    }

    @Override
    public boolean needsToWriteUnicodeChar() {
        return false;
    }
}
