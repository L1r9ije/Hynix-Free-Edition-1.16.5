package su.hynix.managers.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import su.hynix.managers.BaseManager;
import su.hynix.managers.FilePath;

import java.util.*;
import java.util.function.Consumer;

public class WaypointManager extends BaseManager<Map<String, List<WaypointManager.WaypointEntry>>> {

    public WaypointManager() {
        super(FilePath.WAYPOINTS_FILE_REL);
    }

    @Override
    protected void initializeData() {
        this.data = new HashMap<>();
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, List<WaypointEntry>> byServer : data.entrySet()) {
            String server = byServer.getKey();
            JsonArray arr = new JsonArray();
            for (WaypointEntry wp : byServer.getValue()) {
                JsonObject o = new JsonObject();
                o.addProperty("name", wp.name());
                o.addProperty("x", wp.x());
                o.addProperty("y", wp.y());
                o.addProperty("z", wp.z());
                arr.add(o);
            }
            root.add(server, arr);
        }
        return root;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
        data.clear();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String server = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonArray()) continue;
            JsonArray arr = element.getAsJsonArray();
            List<WaypointEntry> list = new ArrayList<>();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                loadProperty(o, "name", n -> loadProperty(o, "x", x -> loadProperty(o, "y", y -> loadProperty(o, "z", z -> {
                    try {
                        list.add(new WaypointEntry(
                                n.getAsString(),
                                x.getAsDouble(),
                                y.getAsDouble(),
                                z.getAsDouble()
                        ));
                    } catch (Exception ignored) {
                    }
                }))));
            }
            data.put(server, list);
        }
    }

    @Override
    protected void handleLoadError(Exception exception) {
        if (data == null) initializeData();
    }

    public List<WaypointEntry> getWaypoints(String serverKey) {
        return new ArrayList<>(data.getOrDefault(serverKey, Collections.emptyList()));
    }

    public void addWaypoint(String serverKey, String name, double x, double y, double z) {
        if (serverKey == null || name == null) return;
        String normalized = name.trim();
        if (normalized.isEmpty()) return;
        List<WaypointEntry> list = data.computeIfAbsent(serverKey, k -> new ArrayList<>());
        list.removeIf(w -> w.name().equalsIgnoreCase(normalized));
        list.add(new WaypointEntry(normalized, x, y, z));
        save();
    }

    public boolean removeWaypoint(String serverKey, String name) {
        if (serverKey == null || name == null) return false;
        List<WaypointEntry> list = data.get(serverKey);
        if (list == null) return false;
        boolean removed = list.removeIf(w -> w.name().equalsIgnoreCase(name.trim()));
        if (removed) save();
        return removed;
    }

    public void clear(String serverKey) {
        if (serverKey == null) return;
        List<WaypointEntry> list = data.get(serverKey);
        if (list != null && !list.isEmpty()) {
            list.clear();
            save();
        }
    }

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) consumer.accept(element);
    }

    public record WaypointEntry(String name, double x, double y, double z) {
    }
}


