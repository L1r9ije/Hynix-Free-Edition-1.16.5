package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.util.text.ITextComponent;
import su.hynix.events.EventContainerTick;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ItemSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.ui.gui.autobuy.ItemList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoBuy extends Module {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d[\\d\\s]*");
    private final List<SellTask> sellQueue = new ArrayList<>();
    private final List<ItemSetting> autosetupQueue = new ArrayList<>();
    private final Map<ItemSetting, Long> autosetupMinPrices = new HashMap<>();
    private final BooleanSetting autosetup = new BooleanSetting("Авто сетап", false);
    private final SliderSetting procent = new SliderSetting("На сколько процентов меньше от самого дешевого предмета", 20, 1, 100, 1);
    private int noMatchTicks = 0;
    private boolean shouldClickSlot0WhenPurchaseScreen = false;
    private ItemSetting lastMatchedSetting = null;
    private boolean pendingSell = false;
    private long pendingSellPrice = 0L;
    private boolean moveInitiated = false;
    private int lastPurchasedCount = 1;
    private int purchaseBatchCooldownTicks = 0;
    private int reopenAhDelayTicks = 0;
    private boolean autosetupRunning = false;
    private int autosetupIndex = 0;
    private int autosetupOpenDelayTicks = 0;
    private long autosetupMinPrice = Long.MAX_VALUE;
    private int autosetupLastPageSeen = -1;
    private boolean autosetupPageProcessed = false;
    private int autosetupPageTurnDelayTicks = 0;
    private boolean autosetupPendingNextClick = false;
    private int autosetupPageBeforeClick = -1;

    public AutoBuy() {
        super("Auto Buy", "Упрощает покупку предметов на сервере HolyWorld", Category.Player);
        addSettings(autosetup, procent);
    }

    public static long extractPrice(ItemStack stack) {
        if (!stack.hasTag()) return 0L;
        CompoundNBT display = stack.getTag().getCompound("display");
        if (!display.contains("Lore", 9)) return 0L;
        ListNBT lore = display.getList("Lore", 8);

        for (int i = 0; i < lore.size(); i++) {
            String text = ITextComponent.Serializer.getComponentFromJsonLenient(lore.getString(i)).getString();
            if (text.toLowerCase().contains("цена за 1")) {
                Long val = extractLastNumber(text);
                if (val != null) return val;
            }
        }
        return 0L;
    }

    private static Long extractLastNumber(String text) {
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        Long lastNumber = null;
        while (matcher.find()) {
            String num = matcher.group().replaceAll("[ \\t]", "");
            if (!num.isEmpty()) lastNumber = Long.parseLong(num);
        }
        return lastNumber;
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || !pendingSell) return;

        if (mc.currentScreen != null) {
            closeCurrentScreen();
            return;
        }


        if (!moveInitiated) {
            int invSlot = findInventorySlotForSetting(lastMatchedSetting);
            if (invSlot >= 0) {
                int sourceCount = mc.player.inventory.getStackInSlot(invSlot).getCount();
                int desired = Math.max(1, lastPurchasedCount);

                if (sourceCount >= desired) {
                    int targetSlot = (sourceCount == desired) ? invSlot : splitStackToExact(invSlot, desired);
                    int currentHotbar = mc.player.inventory.currentItem;
                    if (targetSlot >= 0 && targetSlot < 9 && targetSlot == currentHotbar) {
                        mc.playerController.syncCurrentPlayItem();
                        mc.player.closeScreen();
                        moveInitiated = true;
                        return;
                    }
                    int safeHotbar = findSafeHotbarSlot(mc.player.inventory.getStackInSlot(targetSlot));
                    if (safeHotbar != -1) {
                        mc.player.inventory.currentItem = safeHotbar;
                        mc.playerController.syncCurrentPlayItem();
                    }
                    int windowId = mc.player.openContainer.windowId;
                    int windowSlot = targetSlot < 9 ? targetSlot + 36 : targetSlot;
                    int hotbarIndex = mc.player.inventory.currentItem;
                    mc.playerController.windowClick(windowId, windowSlot, hotbarIndex, ClickType.SWAP, mc.player);
                    mc.playerController.syncCurrentPlayItem();
                    mc.player.closeScreen();
                    moveInitiated = true;
                }
            }
            return;
        }

        mc.player.sendChatMessage("/ah sell " + pendingSellPrice);
        clearSellState();
        if (sellQueue.isEmpty()) reopenAhDelayTicks = 25;
    }

    @EventTarget
    public void onEvent(EventContainerTick e) {
        if (mc.player == null || mc.getConnection() == null) {
            resetState();
            return;
        }
        if (mc.player.openContainer == null && !pendingSell && !autosetup.get()) {
            resetState();
            return;
        }

        if (autosetup.get()) {
            handleAutosetup(e);
            return;
        } else if (autosetupRunning) {
            autosetupRunning = false;
            autosetupQueue.clear();
        }

        if (purchaseBatchCooldownTicks > 0) purchaseBatchCooldownTicks--;
        if (reopenAhDelayTicks > 0 && --reopenAhDelayTicks == 0) mc.player.sendChatMessage("/ah");

        if (shouldClickSlot0WhenPurchaseScreen) {
            String title = getCurrentScreenTitle();
            if (title != null && title.contains("Покупка предмета") && !mc.player.openContainer.inventorySlots.isEmpty()) {
                clickSlot(0);
                shouldClickSlot0WhenPurchaseScreen = false;
                if (lastMatchedSetting != null && lastMatchedSetting.isSellEnabled()) {
                    long unitBuyPrice = lastMatchedSetting.getMaxPrice();
                    long unitSellPrice = unitBuyPrice + Math.round(unitBuyPrice * (lastMatchedSetting.getSellPercent() / 100.0));
                    long totalSellPrice = unitSellPrice * Math.max(1, lastPurchasedCount);
                    sellQueue.add(new SellTask(lastMatchedSetting, lastPurchasedCount, totalSellPrice));
                }
                purchaseBatchCooldownTicks = Math.max(purchaseBatchCooldownTicks, 10);
                return;
            }
        }

        String currentTitle = getCurrentScreenTitle();
        if ((currentTitle == null || !currentTitle.contains("Аукцион")) && !shouldClickSlot0WhenPurchaseScreen) {
            return;
        }

        ItemSetting matchedSetting = null;
        int foundSlot = -1;
        for (int i = 0; i < e.getItems().size(); i++) {
            ItemStack stack = e.getItems().get(i);
            if (stack == null || stack.isEmpty()) continue;

            long price = extractPrice(stack);
            if (price <= 0) continue;

            for (ItemSetting setting : ItemList.getItems()) {
                if (!setting.get()) continue;
                if (setting.getItemStack() != null && stack.getItem() != setting.getItemStack().getItem()) continue;
                if (!matchesRequiredNbt(stack, setting)) continue;
                if (setting.getMaxPrice() > 0 && price <= setting.getMaxPrice()) {
                    foundSlot = i;
                    matchedSetting = setting;
                    lastPurchasedCount = Math.max(1, stack.getCount());
                    break;
                }
            }
            if (foundSlot >= 0) break;
        }

        if (foundSlot >= 0) {
            clickSlot(foundSlot);
            shouldClickSlot0WhenPurchaseScreen = true;
            lastMatchedSetting = matchedSetting;
            noMatchTicks = 0;
            return;
        }

        noMatchTicks++;
        if (noMatchTicks % 9 == 0 && mc.player.openContainer.inventorySlots.size() > 47) {
            String title = getCurrentScreenTitle();
            if (title == null || !title.contains("Покупка предмета")) clickSlot(47);
        }

        if (!pendingSell && !shouldClickSlot0WhenPurchaseScreen && !sellQueue.isEmpty()
                && purchaseBatchCooldownTicks <= 0 && noMatchTicks >= 3) {
            SellTask task = sellQueue.remove(0);
            lastMatchedSetting = task.setting;
            lastPurchasedCount = Math.max(1, task.count);
            pendingSell = true;
            pendingSellPrice = task.price;
            moveInitiated = false;
        }
    }

    private void handleAutosetup(EventContainerTick e) {
        if (!autosetupRunning) {
            autosetupQueue.clear();
            for (ItemSetting setting : ItemList.getItems()) {
                if (setting.get()) autosetupQueue.add(setting);
            }
            autosetupIndex = 0;
            if (!autosetupQueue.isEmpty()) {
                autosetupRunning = true;
                autosetupMinPrices.clear();
                for (ItemSetting s : autosetupQueue) autosetupMinPrices.put(s, Long.MAX_VALUE);
                openAhForCurrentItem();
            }
            return;
        }

        if (autosetupOpenDelayTicks > 0) {
            autosetupOpenDelayTicks--;
            return;
        }
        if (autosetupPageTurnDelayTicks > 0) {
            autosetupPageTurnDelayTicks--;
            return;
        }

        ItemSetting current = autosetupQueue.get(autosetupIndex);

        String title = getCurrentScreenTitle();
        if (title == null || !title.contains("Аукцион")) return;

        int page = 1, pages = 1;
        int lp = title.indexOf('(');
        int rp = title.indexOf(')');
        if (lp >= 0 && rp > lp) {
            String inside = title.substring(lp + 1, rp);
            int slash = inside.indexOf('/');
            if (slash > 0) {
                try {
                    page = Integer.parseInt(inside.substring(0, slash).trim());
                    pages = Integer.parseInt(inside.substring(slash + 1).trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (autosetupLastPageSeen != page) {
            autosetupPageProcessed = false;
            autosetupLastPageSeen = page;
            autosetupPendingNextClick = false;
        }

        if (!autosetupPageProcessed) {
            for (int i = 0; i < e.getItems().size(); i++) {
                ItemStack stack = e.getItems().get(i);
                if (stack == null || stack.isEmpty()) continue;
                long price = extractPrice(stack);
                if (price <= 0) continue;
                if (current.getItemStack() != null && stack.getItem() != current.getItemStack().getItem()) continue;
                if (!matchesRequiredNbt(stack, current)) continue;
                long curMin = autosetupMinPrices.getOrDefault(current, Long.MAX_VALUE);
                if (price < curMin) autosetupMinPrices.put(current, price);
                if (price < autosetupMinPrice) autosetupMinPrice = price;
            }
            autosetupPageProcessed = true;
        }

        if (page < pages) {
            if (!autosetupPendingNextClick) {
                if (mc.player.openContainer != null && mc.player.openContainer.inventorySlots.size() > 50) {
                    clickSlot(50);
                    autosetupPageTurnDelayTicks = 10;
                    autosetupPendingNextClick = true;
                    autosetupPageBeforeClick = page;
                }
                return;
            } else {
                if (autosetupPageTurnDelayTicks > 0) return;
                if (page == autosetupPageBeforeClick) {
                    long finalMin = autosetupMinPrices.getOrDefault(current, autosetupMinPrice);
                    long adjusted = adjustByPercent(finalMin);
                    current.setMaxPrice(adjusted);
                    autosetupIndex++;
                    if (autosetupIndex < autosetupQueue.size()) {
                        openAhForCurrentItem();
                    } else {
                        autosetupRunning = false;
                        autosetupQueue.clear();
                        autosetupIndex = 0;
                        autosetup.set(false);
                        closeCurrentScreen();
                        resetState();
                    }
                    return;
                }
            }
        }

        long finalMin = autosetupMinPrices.getOrDefault(current, autosetupMinPrice);
        long adjusted = adjustByPercent(finalMin);
        current.setMaxPrice(adjusted);

        autosetupIndex++;
        if (autosetupIndex < autosetupQueue.size()) {
            openAhForCurrentItem();
        } else {
            autosetupRunning = false;
            autosetupQueue.clear();
            autosetupIndex = 0;
            autosetup.set(false);
            closeCurrentScreen();
            resetState();
        }
    }

    private void openAhForCurrentItem() {
        ItemSetting current = autosetupQueue.get(autosetupIndex);
        while (true) {
            long known = autosetupMinPrices.getOrDefault(current, Long.MAX_VALUE);
            if (known != Long.MAX_VALUE) {
                current.setMaxPrice(adjustByPercent(known));
                autosetupIndex++;
                if (autosetupIndex >= autosetupQueue.size()) {
                    autosetupRunning = false;
                    autosetupQueue.clear();
                    autosetupIndex = 0;
                    autosetup.set(false);
                    closeCurrentScreen();
                    resetState();
                    return;
                }
                current = autosetupQueue.get(autosetupIndex);
                continue;
            }
            break;
        }

        String name = current.getSearchQuery() != null && !current.getSearchQuery().isEmpty()
                ? current.getSearchQuery()
                : (current.getName() == null ? "" : current.getName());
        if (!name.isEmpty()) {
            mc.player.sendChatMessage("/ah search " + name);
            autosetupOpenDelayTicks = 12;
        }
        autosetupMinPrice = Long.MAX_VALUE;
        autosetupLastPageSeen = -1;
        autosetupPageProcessed = false;
        autosetupPageTurnDelayTicks = 0;
    }

    private long adjustByPercent(long minPrice) {
        if (minPrice <= 0 || minPrice == Long.MAX_VALUE) return 0L;
        float p = procent.get();
        double factor = Math.max(0.0, 1.0 - (p / 100.0));
        long adjusted = (long) Math.floor(minPrice * factor);
        return Math.max(0L, adjusted);
    }

    private void resetState() {
        noMatchTicks = 0;
        shouldClickSlot0WhenPurchaseScreen = false;
        pendingSell = false;
        autosetupRunning = false;
        autosetupQueue.clear();
        autosetupMinPrices.clear();
    }

    private void closeCurrentScreen() {
        mc.displayGuiScreen(null);
        mc.player.closeScreen();
    }

    private String getCurrentScreenTitle() {
        return mc.currentScreen instanceof ContainerScreen
                ? mc.currentScreen.getTitle().getString()
                : null;
    }

    private void clickSlot(int slot) {
        mc.getConnection().sendPacket(
                new CClickWindowPacket(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, ItemStack.EMPTY,
                        mc.player.openContainer.getNextTransactionID(mc.player.inventory))
        );
    }

    private boolean matchesRequiredNbt(ItemStack stack, ItemSetting setting) {
        if (setting.getSearchQuery() != null && !setting.getSearchQuery().isEmpty()) {
            String displayName = stack.getDisplayName() != null ? stack.getDisplayName().getString() : "";
            if (!displayName.toLowerCase().contains(setting.getSearchQuery().toLowerCase())) return false;
        }

        if (setting.getRequiredNbtParameters() != null && !setting.getRequiredNbtParameters().isEmpty()) {
            List<String> loreLines = extractLoreTextLines(stack);
            if (loreLines.isEmpty()) return false;

            Pattern reqWithRoman = Pattern.compile("^(.+?)\\s+([IVXLCDM]+)\\s*$", Pattern.CASE_INSENSITIVE);
            Pattern romanToken = Pattern.compile("\\b[IVXLCDM]+\\b", Pattern.CASE_INSENSITIVE);

            for (String req : setting.getRequiredNbtParameters()) {
                if (req == null) continue;
                String trimmed = req.trim();
                if (trimmed.isEmpty()) continue;

                Matcher m = reqWithRoman.matcher(trimmed);
                boolean satisfied = false;
                if (m.matches()) {
                    String base = m.group(1).trim().toLowerCase();
                    int needLevel = romanToInt(m.group(2));
                    for (String line : loreLines) {
                        String lower = line.toLowerCase();
                        int idx = lower.indexOf(base);
                        if (idx == -1) continue;
                        Matcher rm = romanToken.matcher(lower.substring(idx + base.length()));
                        if (rm.find() && romanToInt(rm.group()) >= needLevel) {
                            satisfied = true;
                            break;
                        }
                    }
                } else {
                    String needle = trimmed.toLowerCase();
                    for (String line : loreLines) {
                        if (line.toLowerCase().contains(needle)) {
                            satisfied = true;
                            break;
                        }
                    }
                }

                if (!satisfied) return false;
            }
        }

        if (stack.hasTag()) {
            CompoundNBT tag = stack.getTag();
            if (!matchesRequiredEnchantments(tag, setting)) return false;
            if (setting.isRequireUnbreakable())
                return tag.getBoolean("Unbreakable") || tag.getByte("Unbreakable") != 0;
        }
        return true;
    }

    private int romanToInt(String roman) {
        if (roman == null) return 0;
        Map<Character, Integer> map = Map.of(
                'I', 1, 'V', 5, 'X', 10, 'L', 50,
                'C', 100, 'D', 500, 'M', 1000
        );
        int sum = 0, prev = 0;
        for (char c : new StringBuilder(roman.toUpperCase().trim()).reverse().toString().toCharArray()) {
            int val = map.getOrDefault(c, 0);
            sum += (val < prev) ? -val : val;
            prev = val;
        }
        return sum;
    }

    private boolean matchesRequiredEnchantments(CompoundNBT tag, ItemSetting setting) {
        Map<String, Integer> req = setting.getRequiredEnchantments();
        if (req == null || req.isEmpty()) return true;

        Map<String, Integer> present = new HashMap<>();
        if (tag.contains("Enchantments", 9)) {
            ListNBT ench = tag.getList("Enchantments", 10);
            for (int i = 0; i < ench.size(); i++) {
                CompoundNBT e = ench.getCompound(i);
                String id = e.getString("id").toLowerCase();
                int lvl = e.getInt("lvl");
                if (!id.isEmpty()) present.put(id, Math.max(present.getOrDefault(id, 0), lvl));
            }
        }
        return req.entrySet().stream().allMatch(n -> present.getOrDefault(n.getKey(), 0) >= n.getValue());
    }

    private List<String> extractLoreTextLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        if (stack.hasTag()) {
            CompoundNBT display = stack.getTag().getCompound("display");
            if (display.contains("Lore", 9)) {
                ListNBT lore = display.getList("Lore", 8);
                for (int i = 0; i < lore.size(); i++) {
                    String text = ITextComponent.Serializer.getComponentFromJsonLenient(lore.getString(i)).getString();
                    if (!text.isEmpty()) lines.add(text);
                }
            }
        }
        return lines;
    }

    private int findInventorySlotForSetting(ItemSetting setting) {
        for (int i = 0; i < 45; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (!s.isEmpty()
                    && (setting.getItemStack() == null || s.getItem() == setting.getItemStack().getItem())
                    && matchesRequiredNbt(s, setting)) return i;
        }
        return -1;
    }

    private int splitStackToExact(int sourceInvSlot, int desiredCount) {
        if (desiredCount <= 0) return -1;
        int windowId = mc.player.openContainer.windowId;
        int src = sourceInvSlot < 9 ? sourceInvSlot + 36 : sourceInvSlot;
        int empty = findEmptyMainInvSlotIndex();
        if (empty == -1) return -1;
        int windowEmpty = empty < 9 ? empty + 36 : empty;

        mc.playerController.windowClick(windowId, src, 0, ClickType.PICKUP, mc.player);
        for (int placed = 0; placed < desiredCount; placed++)
            mc.playerController.windowClick(windowId, windowEmpty, 1, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(windowId, src, 0, ClickType.PICKUP, mc.player);

        return windowEmpty >= 36 ? windowEmpty - 36 : windowEmpty;
    }

    private int findEmptyMainInvSlotIndex() {
        for (int i = 9; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    private int findSafeHotbarSlot(ItemStack toPlace) {
        for (int i = 0; i < 9; i++) if (mc.player.inventory.getStackInSlot(i).isEmpty()) return i;
        for (int i = 0; i < 9; i++)
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()
                    && mc.player.inventory.getStackInSlot(i).getItem() != toPlace.getItem()) return i;
        return -1;
    }

    private void clearSellState() {
        pendingSell = false;
        pendingSellPrice = 0L;
        moveInitiated = false;
        lastMatchedSetting = null;
    }

    private record SellTask(ItemSetting setting, int count, long price) {
    }
}
