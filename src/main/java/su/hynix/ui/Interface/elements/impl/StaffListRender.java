package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import su.hynix.events.EventRender2D;
import su.hynix.handlers.impl.StaffHandler;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.managers.impl.staffmanager.Staff;
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
public class StaffListRender implements ElementRender {
    public static BooleanSetting skins = new BooleanSetting("Отображать скин", true);
    public static BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);
    private final Dragging dragging;
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10);
    private final Map<String, AnimationUtil[]> itemAnimations = new HashMap<>();
    private final Map<String, Staff> lastSeenStaff = new HashMap<>();
    private final AnimationUtil widthAnimation = new AnimationUtil(0.0f, 15);
    private final AnimationUtil heightAnimation = new AnimationUtil(0.0f, 15);

    @Override
    public void render(EventRender2D.Post event) {
        MatrixStack ms = event.getStack();
        float posX = dragging.getX(), posY = dragging.getY();
        List<Staff> activeItems = StaffHandler.getInstance() != null ? StaffHandler.getInstance().getStaff() : new ArrayList<>();

        alphaAnimation.update(mc.currentScreen instanceof ChatScreen || !activeItems.isEmpty() ? 1.0f : 0.0f);
        float globalAlpha = alphaAnimation.getValue();


        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f * globalAlpha);

        List<Staff> dimsItems = new ArrayList<>(activeItems);
        for (String name : itemAnimations.keySet()) {
            boolean exists = activeItems.stream().anyMatch(s -> s.name().equals(name));
            if (!exists) {
                Staff prev = lastSeenStaff.get(name);
                if (prev != null) dimsItems.add(prev);
            }
        }
        float[] dimensions = calculateDimensions(dimsItems);
        float width = dimensions[0];

        RenderUtil.drawBlurredRoundedRectangle(posX, posY, widthAnimation.getValue(), heightAnimation.getValue(), 3, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), globalAlpha);
        Fonts.hynix_icons[16].drawString(ms, "d", posX - 11.5f + widthAnimation.getValue(), posY + 6.5f, textColor);
        Fonts.sf_medium[15].drawString(ms, "Staff online", posX + 3.5f, posY + 5.5f, textColor);

        float baseItemY = posY + 12.5f;
        List<String> toRemove = new ArrayList<>();

        for (int i = 0; i < activeItems.size(); i++) {
            Staff item = activeItems.get(i);
            String key = item.name();
            int finalI = i;
            AnimationUtil[] anims = itemAnimations.computeIfAbsent(key, k -> new AnimationUtil[]{new AnimationUtil(0.0f, 10), new AnimationUtil(-5, 10), new AnimationUtil(finalI * 10, 15)});
            anims[0].update(1.0f);
            anims[1].update(0.0f);
            float targetY = i * 10;
            anims[2].update(targetY);
            lastSeenStaff.put(key, item);
        }

        for (var entry : itemAnimations.entrySet()) {
            String name = entry.getKey();
            boolean stillActive = activeItems.stream().anyMatch(s -> s.name().equals(name));
            if (!stillActive) {
                AnimationUtil[] anims = entry.getValue();
                anims[0].update(0.0f);
                anims[1].update(-5);
                if (anims[0].isDone() && anims[1].isDone()) {
                    toRemove.add(name);
                }
            }
        }

        toRemove.forEach(itemAnimations::remove);

        ScissorUtil.start(posX, posY, widthAnimation.getValue(), heightAnimation.getValue());
        for (var entry : itemAnimations.entrySet()) {
            String name = entry.getKey();
            AnimationUtil[] anims = entry.getValue();
            float itemAlpha = globalAlpha * anims[0].getValue();
            if (itemAlpha <= 0.01f && anims[0].isDone()) continue;

            float itemY = baseItemY + anims[2].getValue();
            ResourceLocation skin;
            Staff item = activeItems.stream().filter(s -> s.name().equals(name)).findFirst().orElse(lastSeenStaff.get(name));
            if (item == null) continue;
            NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(item.name());
            if (playerInfo != null) {
                skin = playerInfo.getLocationSkin();
            } else {
                skin = new ResourceLocation("textures/entity/steve.png");
            }

            int animatedTextColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f * itemAlpha);

            float avatarOffset = skins.get() ? 12.0f : 3;
            if (skins.get()) {
                RenderUtil.drawRoundedHead(skin, null, posX + 2.5f, itemY + 2, 7, 7, 2.5f, itemAlpha);
            }
            float textX = posX + avatarOffset;

            String plainName = TextFormatting.getTextWithoutFormattingCodes(item.prefix().getString());

            Fonts.sf_medium[13].drawText(ms, item.prefix(), textX, itemY + 4.5f, ColorUtil.applyOpacity(-1, itemAlpha));
            Fonts.sf_medium[13].drawString(ms, item.name(), textX + Fonts.sf_medium[13].getWidth(plainName), itemY + 4.5f, animatedTextColor);

            PlayerEntity player = mc.world.getPlayers().stream().filter(p -> p.getName().getString().equals(item.name())).findFirst().orElse(null);
            int baseColor = item.vanished() ? ColorUtil.getColor(255, 60, 60) : (player != null && mc.player.getDistance(player) <= 100.0 ? ColorUtil.getColor(255, 200, 0) : ColorUtil.getColor(60, 255, 60));
            int color = ColorUtil.applyOpacity(baseColor, itemAlpha);
            String baseText = item.vanished() ? "Spec" : player != null && mc.player.getDistance(player) <= 100.0 ? "Near" : "Active";

            Fonts.sf_medium[13].drawString(ms, baseText, textX - avatarOffset + widthAnimation.getValue() - Fonts.sf_medium[13].getWidth(baseText) - 2.5f, itemY + 4.5f, color);
        }
        ScissorUtil.end();

        heightAnimation.update(15 + activeItems.size() * 10);
        widthAnimation.update(width);
        dragging.setHeight(heightAnimation.getValue());
        dragging.setWidth(widthAnimation.getValue());
    }

    private float[] calculateDimensions(List<Staff> dimsItems) {
        float maxTextWidth = 0;


        for (Staff item : dimsItems) {
            PlayerEntity player = mc.world.getPlayers().stream().filter(p -> p.getName().getString().equals(item.name())).findFirst().orElse(null);

            String baseText = item.vanished() ? "Spec" : player != null && mc.player.getDistance(player) <= 100.0 ? "Near" : "Active";
            String plainName = TextFormatting.getTextWithoutFormattingCodes(item.prefix().getString());
            float prefixWidth = Fonts.sf_medium[13].getWidth(plainName);
            float nameWidth = Fonts.sf_medium[13].getWidth(item.name());
            float statusWidth = Fonts.sf_medium[13].getWidth(baseText);
            maxTextWidth = Math.max(maxTextWidth, prefixWidth + nameWidth + statusWidth);
        }
        float avatarBlock = skins.get() ? 9.5f : 0.0f;
        float width = Math.max(60, avatarBlock + maxTextWidth + 15);
        return new float[]{width};
    }
}
