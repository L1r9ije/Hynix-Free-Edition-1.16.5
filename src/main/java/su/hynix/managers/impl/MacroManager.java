package su.hynix.managers.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import su.hynix.managers.BaseManager;
import su.hynix.managers.FilePath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MacroManager extends BaseManager<List<MacroManager.MacroEntry>> {

    public MacroManager() {
        super(FilePath.MACROS_FILE_REL);
    }

    @Override
    protected void initializeData() {
        this.data = new ArrayList<>();
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject config = new JsonObject();
        JsonArray arr = new JsonArray();
        for (MacroEntry entry : data) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", entry.name());
            obj.addProperty("key", entry.keyCode());
            obj.addProperty("cmd", entry.command());
            arr.add(obj);
        }
        config.add("macros", arr);
        return config;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
        data.clear();
        JsonArray arr = jsonObject.getAsJsonArray("macros");
        if (arr != null) {
            for (JsonElement el : arr) {
                if (el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    loadProperty(o, "name", n -> loadProperty(o, "key", k -> loadProperty(o, "cmd", c -> {
                        try {
                            data.add(new MacroEntry(n.getAsString(), k.getAsInt(), c.getAsString()));
                        } catch (Exception ignored) {
                        }
                    })));
                }
            }
        }
    }

    @Override
    protected void handleLoadError(Exception exception) {
        if (data == null) initializeData();
    }

    public void addMacro(String name, int keyCode, String command) {
        if (name == null || command == null) return;
        removeMacro(name);
        data.add(new MacroEntry(name.trim(), keyCode, command));
        save();
    }

    public void removeMacro(String name) {
        if (name == null) return;
        boolean removed = data.removeIf(m -> m.name().equalsIgnoreCase(name.trim()));
        if (removed) save();
    }

    public void clear() {
        if (!data.isEmpty()) {
            data.clear();
            save();
        }
    }

    public List<MacroEntry> getMacros() {
        return new ArrayList<>(data);
    }

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) consumer.accept(element);
    }

    public record MacroEntry(String name, int keyCode, String command) {
    }
}


