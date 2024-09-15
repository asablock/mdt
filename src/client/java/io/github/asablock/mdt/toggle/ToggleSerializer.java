package io.github.asablock.mdt.toggle;

import com.google.gson.*;

import java.io.Reader;
import java.io.Writer;

public class ToggleSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveToggles(Writer writer) throws JsonIOException {
        JsonObject root = new JsonObject();

        root.add("toggles", Toggles.root.encodeJson());

        GSON.toJson(root, writer);
    }

    public static void readToggles(Reader reader) throws JsonIOException, JsonSyntaxException {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

        Toggles.root.decodeJson(root.get("toggles"));
    }
}
