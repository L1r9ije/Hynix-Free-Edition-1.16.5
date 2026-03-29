package su.hynix.managers.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import su.hynix.managers.BaseManager;
import su.hynix.managers.FilePath;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.utils.misc.HasherUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ThemeManager extends BaseManager<ThemeManager> {
    private static final File THEME_DIR = new File(FilePath.BASE_PATH + FilePath.THEMES_DIR_REL);

    public ThemeManager() {
        super(FilePath.THEMES_DIR_REL);
    }

    @Override
    public void init() {
        THEME_DIR.mkdirs();
        super.init();
    }

    @Override
    protected void initializeData() {
    }

    @Override
    protected JsonObject serializeData() {
        return null;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
    }

    public List<String> getThemeNames() {
        File[] files = THEME_DIR.listFiles((dir, name) -> name.endsWith(".file"));
        if (files == null) return Collections.emptyList();

        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName().replace(".file", ""));
        }
        return names;
    }

    public Theme loadTheme(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot load theme: name is null or empty");
            return null;
        }

        File file = new File(THEME_DIR, name.trim() + ".file");
        if (!file.exists()) return null;

        Theme theme = new Theme(name);
        try {
            String fileContent = Files.readString(file.toPath());
            String processedContent = encrypt ? HasherUtil.decrypt(fileContent) : fileContent;
            JsonObject config = new JsonParser().parse(processedContent).getAsJsonObject();

            loadProperty(config, "presetName", v -> theme.presetName = v.getAsString());
            loadProperty(config, "creator", v -> theme.creator = v.getAsString());

            JsonArray colorsArray = config.getAsJsonArray("presetColors");
            if (colorsArray != null) {
                theme.presetColors = new int[colorsArray.size()];
                for (int i = 0; i < colorsArray.size(); i++) {
                    theme.presetColors[i] = colorsArray.get(i).getAsInt();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load theme: " + name + " - " + e.getMessage());
        }
        return theme;
    }

    public void saveTheme(String name, ThemeEditor.ThemePreset preset, String creator) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot save theme: name is null or empty");
            return;
        }
        if (preset == null) {
            System.err.println("Cannot save theme: preset is null");
            return;
        }

        try {
            THEME_DIR.mkdirs();
            File file = new File(THEME_DIR, name.trim() + ".file");

            JsonObject config = new JsonObject();
            config.addProperty("presetName", preset.name());
            config.addProperty("creator", creator != null ? creator : "Unknown");

            JsonArray colorsArray = new JsonArray();
            for (int color : preset.colors()) {
                colorsArray.add(color);
            }
            config.add("presetColors", colorsArray);

            String jsonContent = GSON.toJson(config);
            String outputContent = encrypt ? HasherUtil.encrypt(jsonContent) : jsonContent;
            Files.writeString(file.toPath(), outputContent);

        } catch (Exception e) {
            System.err.println("Failed to save theme: " + name + " - " + e.getMessage());
        }
    }

    public boolean deleteTheme(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot delete theme: name is null or empty");
            return false;
        }

        File file = new File(THEME_DIR, name.trim() + ".file");

        return file.exists() && file.delete();
    }

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    @Getter
    public static class Theme {
        private final String name;
        private final File file;
        private String presetName;
        private int[] presetColors;
        private String creator;

        public Theme(String name) {
            this.name = name;
            this.file = new File(THEME_DIR, name + ".file");
        }
    }
}