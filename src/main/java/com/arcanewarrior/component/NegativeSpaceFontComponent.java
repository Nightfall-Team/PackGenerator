package com.arcanewarrior.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class NegativeSpaceFontComponent implements JsonPackComponent {

    private final Path namespacedPath;

    public NegativeSpaceFontComponent(@NotNull Path namespacedPath) {
        this.namespacedPath = namespacedPath;
    }

    @Override
    public @NotNull JsonElement buildComponent() {
        JsonObject root = new JsonObject();
        JsonArray providers = new JsonArray();
        providers.add(generateNegativeSpaces(-512, 1024, '\uE000'));
        root.add("providers", providers);
        return root;
    }

    private @NotNull JsonObject generateNegativeSpaces(int startOffset, int amount, char startUnicodeChar) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "space");
        JsonObject advances = new JsonObject();
        for (int i = startOffset; i <= startOffset + amount; i++) {
            // Magic code from https://stackoverflow.com/questions/2220366/get-unicode-value-of-a-character
            String unicode = String.format("\\u%04x", startUnicodeChar + i - startOffset);
            advances.addProperty(unicode, i);
        }
        object.add("advances", advances);
        return object;
    }

    @Override
    public @NotNull Path filePath() {
        return namespacedPath.resolve("font").resolve("default.json");
    }
}
