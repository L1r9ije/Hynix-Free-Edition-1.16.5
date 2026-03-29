package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import su.hynix.events.EventRender2D;
import su.hynix.hynix;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.Setting;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.ui.Interface.elements.ElementRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.misc.KeyMapper;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.ScissorUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class KeybindsRender implements ElementRender {
    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);
    private final Dragging dragging;
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10);
    private final Map<KeybindItem, AnimationUtil[]> itemAnimations = new HashMap<>();
    private final AnimationUtil widthAnimation = new AnimationUtil(0.0f, 15);
    private final AnimationUtil heightAnimation = new AnimationUtil(0.0f, 15);

    @Override
    public void render(EventRender2D.Post event) {
        MatrixStack ms = event.getStack();
        float posX = dragging.getX(), posY = dragging.getY();
        List<KeybindItem> activeItems = getActiveItems();

        alphaAnimation.update(mc.currentScreen instanceof ChatScreen || !activeItems.isEmpty() ? 1.0f : 0.0f);
        float globalAlpha = alphaAnimation.getValue();

        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f * globalAlpha);

        List<KeybindItem> dimsItems = new ArrayList<>(activeItems);
        dimsItems.addAll(itemAnimations.keySet());
        float[] dimensions = calculateDimensions(dimsItems);
        float width = dimensions[0];

        RenderUtil.drawBlurredRoundedRectangle(posX, posY, widthAnimation.getValue(), heightAnimation.getValue(), 3, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), globalAlpha);
        Fonts.hynix_icons[16].drawString(ms, "c", posX - 11.5f + widthAnimation.getValue(), posY + 6.5f, textColor);
        Fonts.sf_semibold[15].drawString(ms, "Hotkeys", posX + 3.5f, posY + 5.5f, textColor);

        float baseItemY = posY + 12.5f;
        List<KeybindItem> toRemove = new ArrayList<>();

        for (int i = 0; i < activeItems.size(); i++) {
            KeybindItem item = activeItems.get(i);
            int finalI = i;
            AnimationUtil[] anims = itemAnimations.computeIfAbsent(item, k -> new AnimationUtil[]{new AnimationUtil(0.0f, 10), new AnimationUtil(-5, 10), new AnimationUtil(finalI * 10, 15)});
            anims[0].update(1.0f);
            anims[1].update(0.0f);
            float targetY = i * 10;
            anims[2].update(targetY);
        }

        for (var entry : itemAnimations.entrySet()) {
            if (!activeItems.contains(entry.getKey())) {
                AnimationUtil[] anims = entry.getValue();
                anims[0].update(0.0f);
                anims[1].update(-5);
                if (anims[0].isDone() && anims[1].isDone()) {
                    toRemove.add(entry.getKey());
                }
            }
        }

        toRemove.forEach(itemAnimations::remove);

        ScissorUtil.start(posX, posY, widthAnimation.getValue(), heightAnimation.getValue());
        for (var entry : itemAnimations.entrySet()) {
            KeybindItem item = entry.getKey();
            AnimationUtil[] anims = entry.getValue();
            float itemAlpha = globalAlpha * anims[0].getValue();
            if (itemAlpha <= 0.0f && anims[0].isDone()) continue;

            float itemY = baseItemY + anims[2].getValue();
            String nameText = item.name();
            String bindKey = KeyMapper.getKey(item.bind());
            float bindWidth = Fonts.sf_medium[13].getWidth(bindKey);

            int animatedTextColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f * itemAlpha);

            Fonts.sf_medium[13].drawString(ms, nameText, posX + 3.5f, itemY + 4.5f, animatedTextColor);
            Fonts.sf_medium[13].drawString(ms, bindKey, posX + 3.5f + widthAnimation.getValue() - bindWidth - 7.0f, itemY + 4.5f, animatedTextColor);
        }
        ScissorUtil.end();

        heightAnimation.update(15 + activeItems.size() * 10);
        widthAnimation.update(width);
        dragging.setHeight(heightAnimation.getValue());
        dragging.setWidth(widthAnimation.getValue());
    }

    private List<KeybindItem> getActiveItems() {
        List<KeybindItem> activeItems = new ArrayList<>();
        for (Module module : hynix.getInstance().getModuleManager().getModules()) {
            if (module.getBind() != -100 && module.isEnabled() && module.isKeybindvisible()) {
                activeItems.add(new KeybindItem(module.getName(), module.getBind()));
            }
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof BooleanSetting booleanSetting && booleanSetting.getBind() != -100 && booleanSetting.get() && booleanSetting.isKeybindvisible()) {
                    activeItems.add(new KeybindItem(booleanSetting.getName(), booleanSetting.getBind()));
                }
            }
        }
        return activeItems;
    }

    private float[] calculateDimensions(List<KeybindItem> activeItems) {
        float maxNameWidth = 0, maxBindWidth = 0;
        for (KeybindItem item : activeItems) {
            maxNameWidth = Math.max(maxNameWidth, Fonts.sf_medium[13].getWidth(item.name()));
            maxBindWidth = Math.max(maxBindWidth, Fonts.sf_medium[13].getWidth(KeyMapper.getKey(item.bind())));
        }
        float width = Math.max(50, 3.5f + maxNameWidth + 3.5f + maxBindWidth + 3.5f + 1.0f + 3.5f);
        return new float[]{width, maxBindWidth};
    }

    private record KeybindItem(String name, int bind) {

        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                KeybindItem that = (KeybindItem) o;
                return bind == that.bind && name.equals(that.name);
            }

    }
}