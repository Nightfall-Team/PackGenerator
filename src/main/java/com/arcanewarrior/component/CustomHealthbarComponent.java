package com.arcanewarrior.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class CustomHealthbarComponent implements JsonPackComponent {

    private final Path namespacedPath;
    private final char unicodeStartChar = '\uEFF0';
    private int characterOffset = 0;

    public CustomHealthbarComponent(@NotNull Path namespacedPath) {
        this.namespacedPath = namespacedPath;
    }

    @Override
    public @NotNull JsonElement buildComponent() {
        JsonObject root = new JsonObject();
        JsonArray providers = new JsonArray();
        providers.add(createHealthComponent("healthbar_full.png"));
        providers.add(createHealthComponent("healthbar_empty.png"));
        root.add("providers", providers);
        return root;
    }

    private @NotNull JsonObject createHealthComponent(@NotNull String imagePath) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "bitmap");
        jsonObject.addProperty("file", "nightfall:font/" + imagePath);
        jsonObject.addProperty("ascent", -10);
        jsonObject.addProperty("height", 10);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(String.format("\\u%04x", unicodeStartChar + characterOffset));
        // Increment
        characterOffset++;
        jsonObject.add("chars", jsonArray);
        return jsonObject;
    }

    @Override
    public boolean needsToWriteUnicodeChar() {
        return true;
    }

    @Override
    public @NotNull Path filePath() {
        return namespacedPath.resolve("font").resolve("healthbar.json");
    }
}
