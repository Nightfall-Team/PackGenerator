package com.arcanewarrior.component;

import com.arcanewarrior.ResourcePackConstants;
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
        String supportedFormatsString = "[" + ResourcePackConstants.PACK_FORMAT + ", " + ResourcePackConstants.MAX_SUPPORTED_FORMAT + ']';
        packDetails.addProperty("supported_formats", supportedFormatsString);
        root.add("pack", packDetails);
        return root;
    }

    @Override
    public @NotNull Path filePath() {
        return ResourcePackConstants.WORKING_DIRECTORY.resolve("pack.mcmeta");
    }
}
