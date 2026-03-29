package su.hynix.managers.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import su.hynix.hynix;
import su.hynix.managers.BaseManager;
import su.hynix.managers.FilePath;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.Setting;
import su.hynix.modules.api.constructors.impl.*;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;

import java.util.function.Consumer;

public class ClientConfig extends BaseManager<ClientConfig.ClientConfigData> {

    @Setter
    @Getter
    private static String currentActiveTheme = null;

    public ClientConfig() {
        super(FilePath.CLIENT_CONFIG_FILE_REL);
    }

    @Override
    protected void initializeData() {
        this.data = new ClientConfigData();
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject config = new JsonObject();
        config.add("modules", saveModules());

        if (data.getSelectedTheme() != null) {
            config.addProperty("selectedTheme", data.getSelectedTheme());
        }

        if (data.getSelectedConfig() != null) {
            config.addProperty("selectedConfig", data.getSelectedConfig());
        }

        return config;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
        loadProperty(jsonObject, "selectedTheme", v -> data.setSelectedTheme(v.getAsString()));
        loadProperty(jsonObject, "selectedConfig", v -> data.setSelectedConfig(v.getAsString()));
    }

    public void saveCurrentSettings() {
        try {
            save();
        } catch (Exception e) {
            System.err.println("Ошибка сохранения настроек клиента: " + e.getMessage());
        }
    }

    public void applySettings() {
        if (configFile.exists()) {
            try {
                String fileContent = java.nio.file.Files.readString(configFile.toPath());
                String processedContent = encrypt ? su.hynix.utils.misc.HasherUtil.decrypt(fileContent) : fileContent;
                JsonObject config = new JsonParser().parse(processedContent).getAsJsonObject();

                if (config.has("modules")) {
                    loadModules(config.getAsJsonObject("modules"));
                }

                applyThemeSettings();
                loadSelectedConfig();
            } catch (Exception e) {
                System.err.println("Failed to apply client config settings: " + e.getMessage());
            }
        }
    }

    private void loadModules(JsonObject modules) {
        boolean prev = Module.isSuppressToggleEffects();
        Module.setSuppressToggleEffects(true);
        try {
            hynix.getInstance().getModuleManager().getModules().forEach(module -> {
                JsonObject moduleData = modules.getAsJsonObject(module.getName().toLowerCase());
                if (moduleData == null) return;

                module.settoggled(false);
                loadProperty(moduleData, "key", v -> module.setBind(v.getAsInt()));
                loadProperty(moduleData, "enabled", v -> module.settoggled(v.getAsBoolean()));
                loadProperty(moduleData, "togglemode", v -> {
                    try {
                        module.setToggleMode(Module.ToggleMode.valueOf(v.getAsString()));
                    } catch (Exception e) {
                        module.setToggleMode(Module.ToggleMode.TOGGLE);
                    }
                });
                loadProperty(moduleData, "keybindvisible", v -> module.setKeybindvisible(v.getAsBoolean()));

                module.getSettings().forEach(setting -> loadSetting(moduleData, setting));
            });
        } finally {
            Module.setSuppressToggleEffects(prev);
        }
    }

    private void loadSetting(JsonObject moduleData, Setting<?> setting) {
        JsonElement element = moduleData.get(setting.getName());
        if (element == null || element.isJsonNull()) return;

        try {
            if (setting instanceof SliderSetting) {
                ((SliderSetting) setting).set(element.getAsFloat());
            } else if (setting instanceof BooleanSetting boolSetting) {
                if (element.isJsonObject()) {
                    JsonObject boolData = element.getAsJsonObject();
                    loadProperty(boolData, "value", v -> boolSetting.set(v.getAsBoolean()));
                    loadProperty(boolData, "bind", v -> boolSetting.setBind(v.getAsInt()));
                    loadProperty(boolData, "keybindvisible", v -> boolSetting.setKeybindvisible(v.getAsBoolean()));
                    loadProperty(boolData, "togglemode", v -> {
                        try {
                            boolSetting.setToggleMode(Module.ToggleMode.valueOf(v.getAsString()));
                        } catch (Exception e) {
                            boolSetting.setToggleMode(Module.ToggleMode.TOGGLE);
                        }
                    });
                }
            } else if (setting instanceof ColorSetting) {
                ((ColorSetting) setting).set(element.getAsInt());
            } else if (setting instanceof ModeSetting) {
                ((ModeSetting) setting).set(element.getAsString());
            } else if (setting instanceof BindSetting) {
                ((BindSetting) setting).set(element.getAsInt());
            } else if (setting instanceof StringSetting) {
                ((StringSetting) setting).set(element.getAsString());
            } else if (setting instanceof MultiBooleanSetting multiSetting) {
                if (element.isJsonObject()) {
                    JsonObject multiData = element.getAsJsonObject();
                    multiSetting.get().forEach(option -> {
                        JsonElement optionElement = multiData.get(option.getName());
                        if (optionElement != null && !optionElement.isJsonNull()) {
                            option.set(optionElement.getAsBoolean());
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load setting " + setting.getName() + ": " + e.getMessage());
        }
    }

    private JsonObject saveModules() {
        JsonObject modules = new JsonObject();
        hynix.getInstance().getModuleManager().getModules().forEach(module -> {
            JsonObject moduleData = new JsonObject();
            moduleData.addProperty("key", module.getBind());
            moduleData.addProperty("enabled", module.isEnabled());
            moduleData.addProperty("togglemode", module.getToggleMode().name());
            moduleData.addProperty("keybindvisible", module.isKeybindvisible());

            module.getSettings().forEach(setting -> saveSetting(moduleData, setting));
            modules.add(module.getName().toLowerCase(), moduleData);
        });
        return modules;
    }

    private void saveSetting(JsonObject moduleData, Setting<?> setting) {
        try {
            if (setting instanceof BooleanSetting boolSetting) {
                JsonObject boolData = new JsonObject();
                boolData.addProperty("value", boolSetting.get());
                boolData.addProperty("bind", boolSetting.getBind());
                boolData.addProperty("keybindvisible", boolSetting.isKeybindvisible());
                boolData.addProperty("togglemode", boolSetting.getToggleMode().name());
                moduleData.add(setting.getName(), boolData);
            } else if (setting instanceof SliderSetting) {
                moduleData.addProperty(setting.getName(), ((SliderSetting) setting).get());
            } else if (setting instanceof ModeSetting) {
                moduleData.addProperty(setting.getName(), ((ModeSetting) setting).get());
            } else if (setting instanceof ColorSetting) {
                moduleData.addProperty(setting.getName(), ((ColorSetting) setting).get());
            } else if (setting instanceof BindSetting) {
                moduleData.addProperty(setting.getName(), ((BindSetting) setting).get());
            } else if (setting instanceof StringSetting) {
                moduleData.addProperty(setting.getName(), ((StringSetting) setting).get());
            } else if (setting instanceof MultiBooleanSetting multiSetting) {
                JsonObject multiData = new JsonObject();
                multiSetting.get().forEach(option -> multiData.addProperty(option.getName(), option.get()));
                moduleData.add(setting.getName(), multiData);
            }
        } catch (Exception e) {
            System.err.println("Failed to save setting " + setting.getName() + ": " + e.getMessage());
        }
    }

    public void applyThemeSettings() {
        if (data.getSelectedTheme() == null || data.getSelectedTheme().trim().isEmpty()) {
            return;
        }

        ThemeManager themeManager = hynix.getInstance().getThemeManager();
        if (themeManager == null) return;

        ThemeManager.Theme theme = themeManager.loadTheme(data.getSelectedTheme());
        if (theme == null) {
            String presetThemeName = data.getSelectedTheme();
            if (presetThemeName.startsWith("Имя: ")) {
                presetThemeName = presetThemeName.substring(5);
            }
            theme = themeManager.loadTheme(presetThemeName);
        }

        if (theme != null && theme.getPresetColors() != null) {
            String themePresetName = theme.getPresetName() != null ? theme.getPresetName() : data.getSelectedTheme();
            applyThemeColorsAndUpdatePreset(themePresetName, theme.getPresetColors());
            setCurrentActiveTheme(themePresetName);
        }
    }

    private void applyThemeColors(int[] colors) {
        try {
            ThemeSettings[] settings = ThemeSettings.values();
            for (int i = 0; i < Math.min(colors.length, settings.length); i++) {
                if (ThemeEditor.colorSettings.containsKey(settings[i])) {
                    su.hynix.modules.api.constructors.impl.ColorSetting colorSetting = ThemeEditor.colorSettings.get(settings[i]);
                    Runnable originalCallback = colorSetting.getOnChange();
                    colorSetting.setOnChange(null);
                    colorSetting.set(colors[i]);
                    colorSetting.setOnChange(originalCallback);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка применения цветов темы: " + e.getMessage());
        }
    }

    private void applyThemeColorsAndUpdatePreset(String themeName, int[] colors) {
        try {
            applyThemeColors(colors);
            updateThemeEditorPreset(themeName, colors);
        } catch (Exception e) {
            System.err.println("Ошибка применения темы с preset: " + e.getMessage());
        }
    }

    private void updateThemeEditorPreset(String themeName, int[] colors) {
        try {
            java.lang.reflect.Field currentPresetField = ThemeEditor.class.getDeclaredField("currentPreset");
            currentPresetField.setAccessible(true);

            // newPreset удалён

            if (java.lang.reflect.Modifier.isStatic(currentPresetField.getModifiers())) {
                // currentPresetField.set(null, newPreset); — тоже должен быть удалён, иначе ошибка
            }
        } catch (Exception e1) {
            // Ignore
        }
    }

    public void onThemeApplied(String themeName) {
        try {
            data.setSelectedTheme(themeName);
            setCurrentActiveTheme(themeName);
            save();
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении примененной темы: " + e.getMessage());
        }
    }

    public void onThemeCreated(String themeName) {
        try {
            data.setSelectedTheme(themeName);
            setCurrentActiveTheme(themeName);
            save();
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении созданной темы: " + e.getMessage());
        }
    }

    public void onConfigSelected(String configName) {
        try {
            data.setSelectedConfig(configName);
            save();
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении выбранного конфига: " + e.getMessage());
        }
    }

    public void loadSelectedConfig() {
        if (data.getSelectedConfig() != null && !data.getSelectedConfig().trim().isEmpty()) {
            ConfigManager configManager = hynix.getInstance().getConfigManager();
            if (configManager != null) {
                configManager.loadConfig(data.getSelectedConfig());
            }
        }
    }

    public String getSelectedConfig() {
        return data.getSelectedConfig();
    }

    public void setSelectedConfig(String configName) {
        data.setSelectedConfig(configName);
        save();
    }

    public void initializeDefaultTheme() {
        ThemeManager themeManager = hynix.getInstance().getThemeManager();
        if (themeManager != null) {
            if (data.getSelectedTheme() == null || data.getSelectedTheme().trim().isEmpty()) {
                java.util.List<String> availableThemes = themeManager.getThemeNames();
                if (!availableThemes.isEmpty()) {
                    String defaultTheme = availableThemes.get(0);
                    data.setSelectedTheme(defaultTheme);
                    save();
                }
            }
        }
    }

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    @Getter
    @Setter
    public static class ClientConfigData {
        private String selectedTheme;
        private String selectedConfig;
    }
}