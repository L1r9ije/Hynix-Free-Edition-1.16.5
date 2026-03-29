package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import su.hynix.events.EventRender2D;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.ui.Interface.elements.ElementRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

@RequiredArgsConstructor
public class InventoryRender implements ElementRender {

    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);

    private final Dragging dragging;

    @Override
    public void render(EventRender2D.Post event) {
        MatrixStack ms = event.getStack();
        float posX = dragging.getX(), posY = dragging.getY();

        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f);
        int rectColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SEPARATOR), ThemeEditor.getAlpha(ThemeSettings.SEPARATOR) / 255f);

        RenderUtil.drawBlurredRoundedRectangle(posX, posY, 144, 15, 3, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 1);
        Fonts.icons[16].drawString(ms, "A", posX + 4.5f, posY + 6.5f, textColor);
        RenderUtil.drawMinecraftRectangle(ms, posX + 15.5f, posY + 3, 0.5f, 9, rectColor);
        Fonts.sf_medium[15].drawString(ms, "Inventory", posX + 18.5f, posY + 5.5f, textColor);

        float slotY = posY + 15.5f;
        float slotHeight = 16f;

        int itemIndex = 9;
        float itemSize = 0.5f;

        for (int row = 0; row < 3; row++) {
            RenderUtil.drawBlurredRoundedRectangle(posX, slotY, 144, slotHeight, 4, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 1);

            for (int j = 1; j < 9; j++) {
                float offsetX = posX + j * 16;
                RenderUtil.drawMinecraftRectangle(ms, offsetX - 0.5f, slotY + 2f, 0.5f, slotHeight - 4f, rectColor);
            }

            for (int col = 0; col < 9; col++) {
                if (itemIndex >= 36) break;

                ItemStack stack = mc.player.inventory.mainInventory.get(itemIndex);
                int itemX = (int) (posX + col * 16 + 8 - (8 * itemSize));
                int itemY = (int) (slotY + 8 - (8 * itemSize));

                RenderUtil.drawStack(stack, itemX, itemY, itemSize);

                itemIndex++;
            }

            slotY += slotHeight;
        }

        dragging.setWidth(144);
        dragging.setHeight(63.5f);
    }
}