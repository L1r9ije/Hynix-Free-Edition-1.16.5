package su.hynix.managers.impl.staffmanager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import su.hynix.managers.BaseManager;
import su.hynix.managers.FilePath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StaffManager extends BaseManager<List<StaffManager.StaffEntry>> {

    public StaffManager() {
        super(FilePath.STAFF_FILE_REL);
    }

    @Override
    protected void initializeData() {
        this.data = new ArrayList<>();
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject config = new JsonObject();
        JsonArray arr = new JsonArray();
        for (StaffEntry entry : data) {
            arr.add(new JsonPrimitive(entry.name()));
        }
        config.add("staff", arr);
        return config;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
        data.clear();
        JsonArray arr = jsonObject.getAsJsonArray("staff");
        if (arr != null) {
            for (JsonElement element : arr) {
                if (element.isJsonPrimitive()) {
                    data.add(new StaffEntry(element.getAsString()));
                } else if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    loadProperty(obj, "name", nameEl -> data.add(new StaffEntry(nameEl.getAsString())));
                }
            }
        }
    }

    @Override
    protected void handleLoadError(Exception exception) {
        if (data == null) initializeData();
    }

    public void addStaff(String name) {
        if (name == null || name.trim().isEmpty()) return;
        String n = name.trim();
        if (!isStaff(n)) {
            data.add(new StaffEntry(n));
            save();
        }
    }

    public void removeStaff(String name) {
        if (name == null) return;
        boolean removed = data.removeIf(e -> e.name().equalsIgnoreCase(name.trim()));
        if (removed) save();
    }

    public void clearStaff() {
        if (!data.isEmpty()) {
            data.clear();
            save();
        }
    }

    public boolean isStaff(String name) {
        if (name == null) return false;
        return data.stream().anyMatch(e -> e.name().equalsIgnoreCase(name.trim()));
    }

    public List<StaffEntry> getStaff() {
        return new ArrayList<>(data);
    }

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    public record StaffEntry(String name) {
    }
}


