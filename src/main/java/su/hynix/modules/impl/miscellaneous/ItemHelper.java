package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import su.hynix.events.*;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ColorSetting;
import su.hynix.modules.api.constructors.impl.TextSetting;
import su.hynix.modules.impl.visuals.ShulkerPreview;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.misc.ChatUtil;
import su.hynix.utils.player.InventoryUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;

import java.util.List;

public class ItemHelper extends Module {
    private final BindSetting chorus = new BindSetting("Хорус");
    private final BindSetting golden_apple = new BindSetting("Золотое яблоко");
    private final BindSetting enchant_golden_apple = new BindSetting("Чарка");
    private final BindSetting trident = new BindSetting("Трезубец");
    private final BindSetting exp = new BindSetting("Пузырек опыта");
    private final BindSetting shield = new BindSetting("Щит");
    private final BindSetting instant_health = new BindSetting("Зелье исцеления");

    private final TextSetting health = new TextSetting("Здоровье");

    private final BooleanSetting show_instant_health = new BooleanSetting("Подсвечивать зелье исцеления", false);
    private final ColorSetting color_instant_health = new ColorSetting("Цвет подсветки зелья исцеления", true, ColorUtil.hex("#FF2AB9"), show_instant_health::get);
    private final BooleanSetting show_enchant_golden_apple = new BooleanSetting("Подсвечивать чарки", false);
    private final ColorSetting color_enchant_golden_apple = new ColorSetting("Цвет подсветки чарки", true, ColorUtil.hex("#FFAC93"), show_enchant_golden_apple::get);
    private final BooleanSetting show_golden_apple = new BooleanSetting("Подсвечивать золотые яблоки", false);
    private final ColorSetting color_golden_apple = new ColorSetting("Цвет подсветки золотого яблока", true, ColorUtil.hex("#E7EB56"), show_golden_apple::get);

    private final TextSetting other = new TextSetting("Остальное");

    private final BooleanSetting decreaseCooldown = new BooleanSetting("Уменьшать задержку на предметы", false);
    private final BooleanSetting show_new_items = new BooleanSetting("Подсвечивать только что поднятые предметы", false);
    private final BooleanSetting show_nbt = new BooleanSetting("Отображать nbt предметов", false);

    private final AnimationUtil anim = new AnimationUtil(0.0f, 4);

    public ItemHelper() {
        super("Item Helper", "Позволяет использовать предмет по нажатию на клавишу", Category.Miscellaneous);
        addSettings(health, show_instant_health, color_instant_health, show_enchant_golden_apple, color_enchant_golden_apple, show_golden_apple, color_golden_apple, other, decreaseCooldown, show_nbt);
    }

    @EventTarget
    public void onEvent(EventKey event) {
        if (mc.player == null || !event.isHold()) {
        }

      /*  if (chorus.get() == event.getKey()) {
            InventoryUtil.useItemLegit(Items.CHORUS_FRUIT);
        }

        if (golden_apple.get() == event.getKey()) {
            InventoryUtil.useItemLegit(Items.GOLDEN_APPLE);
        }

        if (enchant_golden_apple.get() == event.getKey()) {
            InventoryUtil.useItemLegit(Items.ENCHANTED_GOLDEN_APPLE);
        }

        if (trident.get() == event.getKey()) {
            InventoryUtil.useItemLegit(Items.TRIDENT);
        }

        if (exp.get() == event.getKey()) {
            InventoryUtil.useItemLegit(Items.EXPERIENCE_BOTTLE);
        }

        if (shield.get() == event.getKey()) {
            InventoryUtil.useItemLegit(Items.SHIELD);
        }

        if (instant_health.get() == event.getKey()) {
            int slot = InventoryUtil.findPotionSlotWithEffects(true, true, true, true, Effects.INSTANT_HEALTH);
            if (slot != -1) InventoryUtil.useItemLegit(Item.getItemById(slot));
        }*/
    }

    @EventTarget
    public void onEvent(EventCooldown event) {
        Item item = event.getItem();
        ItemStack itemStack = new ItemStack(item);
        if (decreaseCooldown.get() && itemStack.isFood()) {
            int reduction = item.getFood().isFastEating() ? 16 : 32;
            int originalTicks = event.getTicks();

            if (originalTicks > reduction && itemStack.getItem() != Items.DRIED_KELP) {
                event.setTicks(originalTicks - reduction);
                IFormattableTextComponent message = new StringTextComponent("Задержка на ").append(new StringTextComponent(item.getName().getString())).append(new StringTextComponent(" уменьшена на ~" + (reduction / 20.0) + " секунды")).setStyle(new StringTextComponent("").getStyle().applyFormatting(TextFormatting.GRAY));
                ChatUtil.addText(message);
            }
        }
    }

    @EventTarget
    public void onEvent(EventContainerRender.Pre event) {
        if (mc.currentScreen instanceof CreativeScreen || !(event.getContainer() instanceof PlayerContainer)) return;

        for (Slot slot : event.getContainer().inventorySlots) {
            if (slot == null || !slot.getHasStack()) continue;
            renderItemHighlight(event.getStack(), slot.getStack(), event.getGuiLeft() + slot.xPos, event.getGuiTop() + slot.yPos);
        }
    }

    @EventTarget
    public void onEvent(EventHotbarRender event) {
        if (mc.currentScreen instanceof CreativeScreen) return;

        int centerX = mc.getMainWindow().getScaledWidth() / 2;
        int baseY = mc.getMainWindow().getScaledHeight() - 16 - 3;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.isEmpty()) continue;
            renderItemHighlight(event.getStack(), stack, centerX - 90 + i * 20 + 2, baseY);
        }
    }

    private void renderItemHighlight(MatrixStack matrixStack, ItemStack stack, float x, float y) {
        int color = getItemHighlightColor(stack);
        if (color != 0) {
            float norm = (anim.getValue() + 0.75f) / 1.5f;
            int alpha = (int) (75 + norm * 100);
            RenderUtil.drawMinecraftRectangle(matrixStack, x, y, 16, 16, ColorUtil.applyOpacity(color, alpha));
        }
    }

    private int getItemHighlightColor(ItemStack stack) {
        if (show_enchant_golden_apple.get() && stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return color_enchant_golden_apple.get();
        }
        if (show_golden_apple.get() && stack.getItem() == Items.GOLDEN_APPLE) {
            return color_golden_apple.get();
        }
        if (show_instant_health.get() && InventoryUtil.stackHasAnyEffect(stack, true, false, false, Effects.INSTANT_HEALTH)) {
            return color_instant_health.get();
        }
        return 0;
    }

    @EventTarget
    public void onEvent(EventRenderTooltip event) {
        if (ShulkerPreview.shouldShowPreview(event.stack)) return;
        if (!show_nbt.get() || !event.stack.hasTag()) return;
        event.setCancelled(true);

        List<ITextComponent> tooltip = mc.currentScreen.getTooltipFromItem(event.stack);
        tooltip.add(StringTextComponent.EMPTY);
        addTag(tooltip, event.stack.getTag(), 0);

        mc.currentScreen.func_243308_b(event.matrixStack, tooltip, event.mouseX, event.mouseY);
    }

    private void addTag(List<ITextComponent> tooltip, CompoundNBT tag, int depth) {
        String indent = "  ".repeat(depth);
        for (String key : tag.keySet()) {
            INBT base = tag.get(key);
            String type = getNbtType(base);
            tooltip.add(new StringTextComponent(indent + "- " + type + ": " + key).mergeStyle(TextFormatting.DARK_GRAY));
        }
    }

    private String getNbtType(INBT nbt) {
        if (nbt instanceof ByteNBT) return "byte";
        if (nbt instanceof ShortNBT) return "short";
        if (nbt instanceof IntNBT) return "int";
        if (nbt instanceof LongNBT) return "long";
        if (nbt instanceof FloatNBT) return "float";
        if (nbt instanceof DoubleNBT) return "double";
        if (nbt instanceof StringNBT) return "string";
        if (nbt instanceof ByteArrayNBT) return "byte[]";
        if (nbt instanceof IntArrayNBT) return "int[]";
        if (nbt instanceof LongArrayNBT) return "long[]";
        if (nbt instanceof ListNBT) return "list";
        if (nbt instanceof CompoundNBT) return "compound";
        return "unknown";
    }
}