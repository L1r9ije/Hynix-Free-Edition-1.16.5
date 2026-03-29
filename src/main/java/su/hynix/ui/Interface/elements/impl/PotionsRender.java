package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import su.hynix.events.EventRender2D;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.ui.Interface.elements.ElementRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.ScissorUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PotionsRender implements ElementRender {
    private static final int DURATION_HIDE_TICKS = 60 * 60 * 20;
    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);
    public static BooleanSetting badeffects = new BooleanSetting("Отображать плохие эффекты", true);
    private final Dragging dragging;
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10);
    private final Map<PotionItem, AnimationUtil[]> itemAnimations = new HashMap<>();
    private final Map<PotionItem, String> lastDurationTexts = new HashMap<>();
    private final Map<PotionItem, Integer> lastDurationTicks = new HashMap<>();
    private final AnimationUtil widthAnimation = new AnimationUtil(0.0f, 15);
    private final AnimationUtil heightAnimation = new AnimationUtil(0.0f, 15);

    @Override
    public void render(EventRender2D.Post event) {
        MatrixStack ms = event.getStack();
        float posX = dragging.getX(), posY = dragging.getY();
        List<PotionItem> activeItems = getActiveItems();

        alphaAnimation.update(mc.currentScreen instanceof ChatScreen || !activeItems.isEmpty() ? 1.0f : 0.0f);
        float globalAlpha = alphaAnimation.getValue();

        Map<PotionItem, EffectInstance> effectInstances = new HashMap<>();
        for (EffectInstance instance : mc.player.getActivePotionEffects()) {
            PotionItem item = new PotionItem(instance.getPotion(), instance.getAmplifier());
            effectInstances.put(item, instance);
            String durText = instance.getDuration() > DURATION_HIDE_TICKS ? "**:**" : EffectUtils.getPotionDurationString(instance, 1);
            lastDurationTexts.put(item, durText);
            lastDurationTicks.put(item, instance.getDuration());
        }

        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f * globalAlpha);

        List<PotionItem> dimsItems = new ArrayList<>(activeItems);
        dimsItems.addAll(itemAnimations.keySet());
        float[] dimensions = calculateDimensions(dimsItems, effectInstances);
        float width = dimensions[0];

        RenderUtil.drawBlurredRoundedRectangle(posX, posY, widthAnimation.getValue(), heightAnimation.getValue(), 3, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), globalAlpha);
        Fonts.hynix_icons[15].drawString(ms, "f", posX - 10.5f + widthAnimation.getValue(), posY + 6.5f, textColor);
        Fonts.sf_medium[15].drawString(ms, "Potions", posX + 3.5f, posY + 5.5f, textColor);

        float baseItemY = posY + 12.5f;
        List<PotionItem> toRemove = new ArrayList<>();

        for (int i = 0; i < activeItems.size(); i++) {
            PotionItem item = activeItems.get(i);
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

        toRemove.forEach(item -> {
            itemAnimations.remove(item);
            lastDurationTexts.remove(item);
            lastDurationTicks.remove(item);
        });

        ScissorUtil.start(posX, posY, widthAnimation.getValue(), heightAnimation.getValue());
        for (var entry : itemAnimations.entrySet()) {
            PotionItem item = entry.getKey();
            AnimationUtil[] anims = entry.getValue();
            float itemAlpha = globalAlpha * anims[0].getValue();
            if (itemAlpha <= 0.01f && anims[0].isDone()) continue;

            float itemY = baseItemY + anims[2].getValue();
            EffectInstance instance = effectInstances.get(item);
            String nameText = item.getEffectName();
            int durationTicks = instance != null ? instance.getDuration() : lastDurationTicks.getOrDefault(item, 0);
            String durationText = durationTicks > DURATION_HIDE_TICKS ? "**:**" : (instance != null ? EffectUtils.getPotionDurationString(instance, 1) : lastDurationTexts.getOrDefault(item, ""));
            boolean isLowDuration = durationTicks > 0 && durationTicks <= 120;
            float pulseValue = isLowDuration ? 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 150.0) : 1.0f;
            float durationWidth = Fonts.sf_medium[13].getWidth(durationText);

            int animatedTextColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f * itemAlpha * pulseValue);
            int animatedRectColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.SEPARATOR), ThemeEditor.getAlpha(ThemeSettings.SEPARATOR) / 255f * itemAlpha * pulseValue);

            Fonts.sf_medium[13].drawString(ms, nameText, posX + 3.5f + 5.0f + 2.5f + 2, itemY + 4.5f, animatedTextColor);
            Fonts.sf_medium[13].drawString(ms, durationText, posX + widthAnimation.getValue() - 3.0f - durationWidth, itemY + 4.5f, animatedTextColor);
            RenderUtil.drawMinecraftRectangle(ms, posX + 10.5f, itemY + 3, 0.5f, 5, animatedRectColor);
            RenderUtil.drawImage2D(item.effect.getEffectTexture(), posX + 2.5f, itemY + 2, 7, 7, ColorUtil.applyOpacity(-1, itemAlpha * pulseValue));
        }
        ScissorUtil.end();

        heightAnimation.update(15 + activeItems.size() * 10);
        widthAnimation.update(width);
        dragging.setHeight(heightAnimation.getValue());
        dragging.setWidth(widthAnimation.getValue());
    }

    private List<PotionItem> getActiveItems() {
        List<PotionItem> activeItems = new ArrayList<>();
        for (EffectInstance instance : mc.player.getActivePotionEffects()) {
            Effect effect = instance.getPotion();
            if (badeffects.get() || effect.getEffectType() != EffectType.HARMFUL) {
                activeItems.add(new PotionItem(effect, instance.getAmplifier()));
            }
        }
        return activeItems;
    }

    private float[] calculateDimensions(List<PotionItem> dimsItems, Map<PotionItem, EffectInstance> effectInstances) {
        float maxNameWidth = 0, maxDurationWidth = 0;
        for (PotionItem item : dimsItems) {
            maxNameWidth = Math.max(maxNameWidth, Fonts.sf_medium[13].getWidth(item.getEffectName()));
            String duration;
            if (effectInstances.containsKey(item))
                duration = effectInstances.get(item).getDuration() > DURATION_HIDE_TICKS ? "**:**" : EffectUtils.getPotionDurationString(effectInstances.get(item), 1);
            else
                duration = lastDurationTicks.getOrDefault(item, 0) > DURATION_HIDE_TICKS ? "**:**" : lastDurationTexts.getOrDefault(item, "");
            maxDurationWidth = Math.max(maxDurationWidth, Fonts.sf_medium[13].getWidth(duration));
        }
        float width = Math.max(48, 3.5f + maxNameWidth + 3.5f + maxDurationWidth + 3.5f + 1.0f + 3.5f + 6);
        return new float[]{width};
    }

    private record PotionItem(Effect effect, int amplifier) {

        public String getEffectName() {
                return I18n.format(effect.getName()) + (amplifier > 0 ? " " + (amplifier + 1) : "");
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                PotionItem that = (PotionItem) o;
                return amplifier == that.amplifier && effect.equals(that.effect);
            }

    }
}