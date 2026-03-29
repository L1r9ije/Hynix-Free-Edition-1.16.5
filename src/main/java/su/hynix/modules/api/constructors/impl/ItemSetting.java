package su.hynix.modules.api.constructors.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import su.hynix.modules.api.constructors.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ItemSetting extends Setting<Boolean> {
    private final ItemStack itemStack;
    private final List<String> requiredNbtParameters = new ArrayList<>();
    private final Map<String, Integer> requiredEnchantments = new HashMap<>();
    private String nbt = "";
    private long maxPrice = 0L;
    private boolean sellEnabled = false;
    private int sellPercent = 0;
    private boolean requireUnbreakable = false;
    private String searchQuery = null;

    public ItemSetting(ItemStack itemStack, String name, Boolean defaultVal) {
        super(name, defaultVal);
        this.itemStack = itemStack;
    }

    public ItemSetting addNBTparametr(String... params) {
        if (params == null) return this;
        for (String p : params) {
            if (p == null) continue;
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                this.requiredNbtParameters.add(trimmed);
            }
        }
        return this;
    }

    public ItemSetting addEnchantment(Enchantment enchantment, int level) {
        if (enchantment == null) return this;
        ResourceLocation key = Registry.ENCHANTMENT.getKey(enchantment);
        if (key != null) {
            this.requiredEnchantments.put(key.toString().toLowerCase(), Math.max(1, level));
        }
        return this;
    }

    public ItemSetting requireUnbreakable() {
        this.requireUnbreakable = true;
        return this;
    }

    public ItemSetting searchQuery(String query) {
        this.searchQuery = query == null ? null : query.trim();
        return this;
    }

    public void setMaxPriceFromString(String priceText) {
        if (priceText == null) {
            this.maxPrice = 0L;
            return;
        }
        String digits = priceText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            this.maxPrice = 0L;
            return;
        }
        try {
            this.maxPrice = Long.parseLong(digits);
        } catch (NumberFormatException e) {
            this.maxPrice = 0L;
        }
    }

    public void setSellPercentFromString(String percentText) {
        if (percentText == null) {
            this.sellPercent = 0;
            return;
        }
        String digits = percentText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            this.sellPercent = 0;
            return;
        }
        try {
            this.sellPercent = Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            this.sellPercent = 0;
        }
    }
}
