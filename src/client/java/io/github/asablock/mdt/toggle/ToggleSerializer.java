package io.github.asablock.mdt.toggle;

import com.google.gson.*;
import io.github.asablock.mdt.Mdt;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public class ToggleSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveToggles(Writer writer) throws JsonIOException {
        JsonObject root = new JsonObject();

        JsonObject values = new JsonObject();
        for (Toggle<?> value : Toggles.TOGGLES.values()) {
            values.add(value.name, value.encodeJson());
        }

        root.add("values", values);

        GSON.toJson(root, writer);
    }

    public static void readToggles(Reader reader) throws JsonIOException, JsonSyntaxException {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

        JsonObject values = root.getAsJsonObject("values");
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            String name = entry.getKey();
            Toggle<?> toggle = Toggles.TOGGLES.get(name);
            if (toggle == null) {
                Mdt.LOGGER.warn("Unknown toggle entry: {}", name);
            } else {
                toggle.decodeJson(entry.getValue());
            }
        }
    }
}
