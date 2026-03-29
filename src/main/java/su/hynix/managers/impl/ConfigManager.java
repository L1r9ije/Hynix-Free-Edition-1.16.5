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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ConfigManager extends BaseManager<ConfigManager.ConfigData> {
    private static final File CONFIG_DIR = new File(FilePath.BASE_PATH + FilePath.CUSTOM_DIR_REL);

    public ConfigManager() {
        super(FilePath.CUSTOM_DIR_REL);
    }

    @Override
    public void init() {
        CONFIG_DIR.mkdirs();
        super.init();
    }

    @Override
    protected void initializeData() {
        this.data = new ConfigData();
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject config = new JsonObject();
        if (data.getLastLoadedConfig() != null) {
            config.addProperty("lastLoadedConfig", data.getLastLoadedConfig());
        }
        return config;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
        if (jsonObject.has("lastLoadedConfig") && !jsonObject.get("lastLoadedConfig").isJsonNull()) {
            data.setLastLoadedConfig(jsonObject.get("lastLoadedConfig").getAsString());
        }
    }

    public List<String> getConfigNames() {
        File[] files = CONFIG_DIR.listFiles((dir, name) -> name.endsWith(".cfg"));
        if (files == null) return Collections.emptyList();

        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(file.getName().replace(".cfg", ""));
        }
        return names;
    }

    public void loadConfig(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot load config: name is null or empty");
            return;
        }

        File file = new File(CONFIG_DIR, name.trim() + ".cfg");
        if (!file.exists()) return;

        try {
            String fileContent = Files.readString(file.toPath());
            String processedContent = encrypt ? su.hynix.utils.misc.HasherUtil.decrypt(fileContent) : fileContent;

            JsonObject config = new JsonParser().parse(processedContent).getAsJsonObject();
            if (config.has("module")) {
                loadModules(config.getAsJsonObject("module"));
                data.setLastLoadedConfig(name);
                save();

                ClientConfig clientConfig = hynix.getInstance().getClientConfig();
                if (clientConfig != null) {
                    clientConfig.onConfigSelected(name);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load config: " + name + " - " + e.getMessage());
        }
    }

    public void saveConfig(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot save config: name is null or empty");
            return;
        }

        CONFIG_DIR.mkdirs();
        File file = new File(CONFIG_DIR, name.trim() + ".cfg");

        try {
            JsonObject config = new JsonObject();
            config.add("module", saveModules());
            config.addProperty("creationDate", System.currentTimeMillis());
            config.addProperty("creator", "HynixUser");

            String jsonContent = GSON.toJson(config);
            String outputContent = encrypt ? su.hynix.utils.misc.HasherUtil.encrypt(jsonContent) : jsonContent;
            Files.writeString(file.toPath(), outputContent);

        } catch (Exception e) {
            System.err.println("Failed to save config: " + name + " - " + e.getMessage());
        }
    }

    public boolean deleteConfig(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot delete config: name is null or empty");
            return false;
        }

        boolean deleted = new File(CONFIG_DIR, name.trim() + ".cfg").delete();
        if (deleted && name.equals(data.getLastLoadedConfig())) {
            data.setLastLoadedConfig(null);
            save();
        }
        return deleted;
    }

    public Config getConfigInfo(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot get config info: name is null or empty");
            return null;
        }

        File file = new File(CONFIG_DIR, name.trim() + ".cfg");
        if (!file.exists()) return null;

        try {
            String fileContent = Files.readString(file.toPath());
            String processedContent = encrypt ? su.hynix.utils.misc.HasherUtil.decrypt(fileContent) : fileContent;

            JsonObject config = new JsonParser().parse(processedContent).getAsJsonObject();
            long creationDate = config.has("creationDate") && !config.get("creationDate").isJsonNull() ? config.get("creationDate").getAsLong() : System.currentTimeMillis();
            String creator = config.has("creator") && !config.get("creator").isJsonNull() ? config.get("creator").getAsString() : "Unknown";
            return new Config(name, creationDate, creator);
        } catch (Exception e) {
            return new Config(name);
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

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    public void resetToDefaults() {
        boolean prev = Module.isSuppressToggleEffects();
        Module.setSuppressToggleEffects(true);
        try {
            hynix.getInstance().getModuleManager().getModules().forEach(module -> {
                module.settoggled(false);
                module.setBind(-100);
                module.setToggleMode(Module.ToggleMode.TOGGLE);
                module.setKeybindvisible(true);

                module.getSettings().forEach(setting -> {
                    try {
                        if (setting instanceof BooleanSetting s) {
                            s.set(s.defaultVal);
                            s.setBind(-100);
                            s.setToggleMode(Module.ToggleMode.TOGGLE);
                            s.setKeybindvisible(true);
                        } else if (setting instanceof SliderSetting s) {
                            s.set(s.defaultVal);
                        } else if (setting instanceof ModeSetting s) {
                            s.set(s.defaultVal);
                        } else if (setting instanceof ColorSetting s) {
                            s.set(s.defaultVal);
                        } else if (setting instanceof BindSetting s) {
                            s.set(-1);
                        } else if (setting instanceof StringSetting s) {
                            s.set("");
                        } else if (setting instanceof MultiBooleanSetting s) {
                            s.get().forEach(opt -> opt.set(opt.defaultVal));
                        }
                    } catch (Exception ignored) {
                    }
                });
            });
        } finally {
            Module.setSuppressToggleEffects(prev);
        }
        ClientConfig clientConfig = hynix.getInstance().getClientConfig();
        if (clientConfig != null) {
            clientConfig.saveCurrentSettings();
        }
    }

    @Getter
    public static class Config {
        private final String name;
        private final File file;
        private final long creationDate;
        private final String creator;

        public Config(String name) {
            this.name = name;
            this.file = new File(CONFIG_DIR, name + ".cfg");
            this.creationDate = System.currentTimeMillis();
            this.creator = "HynixUser" != null ? "HynixUser" : "Unknown";
        }

        public Config(String name, long creationDate, String creator) {
            this.name = name;
            this.file = new File(CONFIG_DIR, name + ".cfg");
            this.creationDate = creationDate;
            this.creator = creator != null ? creator : "Unknown";
        }
    }

    @Getter
    @Setter
    public static class ConfigData {
        private String lastLoadedConfig;
    }
}