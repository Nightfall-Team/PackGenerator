package com.arcanewarrior.component;

import com.arcanewarrior.ResourcePackConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class McMetaComponent implements JsonPackComponent {

    @Override
    public @NotNull JsonElement buildComponent() {
        JsonObject root = new JsonObject();
        JsonObject packDetails = new JsonObject();
        packDetails.addProperty("description", ResourcePackConstants.PACK_DESCRIPTION);
        packDetails.addProperty("pack_format", ResourcePackConstants.PACK_FORMAT);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(ResourcePackConstants.PACK_FORMAT);
        jsonArray.add(ResourcePackConstants.MAX_SUPPORTED_FORMAT);
        packDetails.add("supported_formats", jsonArray);
        root.add("pack", packDetails);
        return root;
    }

    @Override
    public boolean needsToWriteUnicodeChar() {
        return false;
    }

    @Override
    public @NotNull Path filePath() {
        return ResourcePackConstants.WORKING_DIRECTORY.resolve("pack.mcmeta");
    }
}
