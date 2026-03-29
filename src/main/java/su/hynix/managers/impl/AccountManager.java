package su.hynix.managers.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import su.hynix.managers.BaseManager;
import su.hynix.managers.FilePath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static su.hynix.utils.Wrapper.mc;

public class AccountManager extends BaseManager<AccountManager.AccountData> {

    public AccountManager() {
        super(FilePath.ACCOUNTS_FILE_REL);
    }

    public void init() {
        createDirectoriesIfNeeded();
        initializeData();
        load();
        String selectedAccount = getSelectedAccount();
        if (selectedAccount != null && !selectedAccount.isEmpty()) {
            try {
                mc.session.setUsername(selectedAccount);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void initializeData() {
        this.data = new AccountData();
    }

    @Override
    protected JsonObject serializeData() {
        JsonObject config = new JsonObject();
        JsonArray accountsArray = new JsonArray();

        for (Map.Entry<String, Long> entry : data.getAccountNames().entrySet()) {
            String name = entry.getKey();
            long timestamp = entry.getValue();
            boolean isFavorite = data.getFavoriteAccounts().getOrDefault(name, false);

            JsonObject accountObj = new JsonObject();
            accountObj.addProperty("name", name);
            accountObj.addProperty("timestamp", timestamp);
            accountObj.addProperty("isFavorite", isFavorite);
            accountsArray.add(accountObj);
        }

        config.add("accounts", accountsArray);
        if (data.getSelectedAccount() != null) {
            config.addProperty("selectedAccount", data.getSelectedAccount());
        }

        return config;
    }

    @Override
    protected void deserializeData(JsonObject jsonObject) {
        data.getAccountNames().clear();
        data.getFavoriteAccounts().clear();
        data.setSelectedAccount(null);

        JsonArray accountsArray = jsonObject.getAsJsonArray("accounts");
        if (accountsArray != null) {
            for (JsonElement element : accountsArray) {
                if (element.isJsonObject()) {
                    JsonObject accountObj = element.getAsJsonObject();
                    try {
                        loadProperty(accountObj, "name", nameElement -> {
                            loadProperty(accountObj, "timestamp", timestampElement -> {
                                loadProperty(accountObj, "isFavorite", favoriteElement -> {
                                    String name = nameElement.getAsString();
                                    long timestamp = timestampElement.getAsLong();
                                    boolean isFavorite = favoriteElement.getAsBoolean();
                                    data.getAccountNames().put(name, timestamp);
                                    data.getFavoriteAccounts().put(name, isFavorite);
                                });
                            });
                        });
                    } catch (Exception e) {
                        System.err.println("Failed to parse account entry: " + e.getMessage());
                    }
                }
            }
        }

        loadProperty(jsonObject, "selectedAccount", v -> data.setSelectedAccount(v.getAsString()));
    }

    public void addAccount(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Cannot add account: name is null or empty");
            return;
        }

        String accountName = name.trim();
        data.getAccountNames().put(accountName, System.currentTimeMillis());
        data.getFavoriteAccounts().putIfAbsent(accountName, false);
        save();
    }

    public boolean removeAccount(String name) {
        if (name == null) {
            System.err.println("Cannot remove account: name is null");
            return false;
        }

        boolean removed = data.getAccountNames().containsKey(name);
        if (removed) {
            data.getAccountNames().remove(name);
            data.getFavoriteAccounts().remove(name);
            if (name.equals(data.getSelectedAccount())) {
                data.setSelectedAccount(null);
            }
            save();
        }
        return removed;
    }

    public void setFavorite(String name, boolean favorite) {
        if (data.getAccountNames().containsKey(name)) {
            data.getFavoriteAccounts().put(name, favorite);
            save();
        }
    }

    public boolean isFavorite(String name) {
        return data.getFavoriteAccounts().getOrDefault(name, false);
    }

    public List<String> getAccountNamesList() {
        return new ArrayList<>(data.getAccountNames().keySet());
    }

    public void selectAccount(String name) {
        if (data.getAccountNames().containsKey(name)) {
            data.setSelectedAccount(name);
            save();
        }
    }

    public Map<String, Long> getAccountNames() {
        return data.getAccountNames();
    }

    public Map<String, Boolean> getFavoriteAccounts() {
        return data.getFavoriteAccounts();
    }

    public String getSelectedAccount() {
        return data.getSelectedAccount();
    }

    private void loadProperty(JsonObject object, String key, Consumer<JsonElement> consumer) {
        JsonElement element = object.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    @Getter
    @Setter
    public static class AccountData {
        private final Map<String, Long> accountNames = new LinkedHashMap<>();
        private final Map<String, Boolean> favoriteAccounts = new LinkedHashMap<>();
        private String selectedAccount;
    }
}