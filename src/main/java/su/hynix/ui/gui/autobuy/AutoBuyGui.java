package su.hynix.ui.gui.autobuy;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ItemSetting;
import su.hynix.modules.api.constructors.impl.StringSetting;
import su.hynix.modules.api.elements.impl.BooleanElement;
import su.hynix.modules.api.elements.impl.ItemElement;
import su.hynix.modules.api.elements.impl.StringElement;
import su.hynix.ui.gui.Panel;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.Wrapper;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.math.NumberUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.ScissorUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.List;

public class AutoBuyGui extends Panel implements Wrapper {
    private final List<StringElement> stringElements = new ArrayList<>();
    private final List<ItemElement> itemElements = new ArrayList<>();
    private final List<StringElement> priceElements = new ArrayList<>();
    private final List<BooleanElement> sellToggleElements = new ArrayList<>();
    private final List<StringElement> sellPriceElements = new ArrayList<>();
    private final AnimationUtil listScrollAnimation = new AnimationUtil(0f, 10, Easings.SINE_OUT);
    private float listScroll = 0f;
    private float animatedListScroll = 0f;
    private float listMaxHeight = 0f;
    private int selectedIndex = -1;

    public AutoBuyGui() {
        stringElements.add(new StringElement(new StringSetting("Название | !вкл | !выкл")));
        initItemSettings();
    }


    private void initItemSettings() {
        for (ItemSetting setting : ItemList.getItems()) {
            itemElements.add(new ItemElement(setting));
            sellToggleElements.add(new BooleanElement(new BooleanSetting("Продавать", false)));
            priceElements.add(new StringElement(new StringSetting("Цена за штуку")));
            sellPriceElements.add(new StringElement(new StringSetting("От цены покупки")));
        }
        if (!itemElements.isEmpty()) {
            itemElements.get(0).setSelected(true);
            selectedIndex = 0;
        }
    }


    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY, float alpha) {
        listScrollAnimation.update(listScroll);
        animatedListScroll = listScrollAnimation.getValue();

        RenderUtil.drawBlurredRoundedRectangle(x, y, getWidth(), getHeight(), 8, ThemeEditor.getColor(ThemeSettings.MAIN), 1);
        RenderUtil.drawOutlineRectangle(x, y, getWidth(), getHeight(), 8, ThemeEditor.getColor(ThemeSettings.OUTLINE), ThemeEditor.getAlpha(ThemeSettings.OUTLINE));

        int startX = (int) (x + (getWidth() - Fonts.sf_medium[26].getWidth("Auto Buy")) / 2);
        int startY = (int) (y - Fonts.sf_medium[26].getHeight() / 2 + 13);
        Fonts.sf_medium[26].drawString(stack, "Auto Buy", startX, startY, ThemeEditor.getColor(ThemeSettings.HEADER));

        for (StringElement elem : stringElements) {
            elem.setX(x + 1);
            elem.setY(y + 21f);
            elem.setWidth(133);
            elem.render(stack, mouseX, mouseY, alpha);
        }

        List<ItemElement> filteredItems = getFilteredItems();

        float itemBaseY = y + 41f;
        float viewportHeight = getHeight() - 84f;
        float itemSpacing = 18f;

        listMaxHeight = filteredItems.size() * itemSpacing;
        if (listMaxHeight > viewportHeight) {
            listScroll = MathHelper.clamp(listScroll, -listMaxHeight + viewportHeight, 0);
            animatedListScroll = MathHelper.clamp(animatedListScroll, -listMaxHeight + viewportHeight, 0);
        } else {
            listScroll = animatedListScroll = 0;
        }

        ScissorUtil.start(x, itemBaseY - 1, getWidth(), viewportHeight - 2);

        for (int i = 0; i < filteredItems.size(); i++) {
            ItemElement elem = filteredItems.get(i);
            float elemY = itemBaseY + i * itemSpacing + animatedListScroll;
            elem.setX(x + 6);
            elem.setY(elemY);
            elem.setWidth(getWidth() - 12f);
            elem.render(stack, mouseX, mouseY, alpha);
        }

        ScissorUtil.end();

        if (listMaxHeight > viewportHeight) {
            float thumbHeight = Math.max(20f, (viewportHeight / listMaxHeight) * viewportHeight);
            float thumbPosition = (-animatedListScroll / (listMaxHeight - viewportHeight)) * (viewportHeight - thumbHeight);
            RenderUtil.drawRoundedRectangle(x + getWidth() - 4, itemBaseY - 0.5f + thumbPosition, 2, thumbHeight - 2f, 1, ThemeEditor.getColor(ThemeSettings.SCROLL_BAR));
        }

        if (selectedIndex != -1) {
            float labelX = x + 6;
            float labelY = y + getHeight() - 41f;

            BooleanElement sellToggle = sellToggleElements.get(selectedIndex);
            sellToggle.setX(x);
            sellToggle.setY(labelY);
            sellToggle.setWidth(getWidth() - 0.5f);
            sellToggle.render(stack, mouseX, mouseY, alpha);

            float buyY = labelY + 7;
            Fonts.sf_regular[14].drawString(stack, "Цена покупки", labelX, buyY + 8, ThemeEditor.getColor(ThemeSettings.TEXT));
            StringElement priceElem = priceElements.get(selectedIndex);
            priceElem.setX(labelX + 46);
            priceElem.setY(buyY);
            priceElem.setWidth(82f);
            priceElem.render(stack, mouseX, mouseY, alpha);
            ItemSetting sel = itemElements.get(selectedIndex).getSetting();
            if (!priceElem.isFocused) {
                long cur = sel.getMaxPrice();
                String desired = cur <= 0 ? "" : String.valueOf(cur);
                String formatted = NumberUtil.formatThousands(desired);
                if (!formatted.equals(priceElem.getInputText())) {
                    priceElem.setInputText(formatted);
                }
            }
            sel.setMaxPriceFromString(priceElem.getInputText());
            sel.setSellEnabled(sellToggle.getSetting().get());

            float sellY = labelY + 21;
            StringElement sellPriceElem = sellPriceElements.get(selectedIndex);
            Fonts.sf_regular[14].drawString(stack, "% Продажи", labelX, sellY + 8, ThemeEditor.getColor(ThemeSettings.TEXT));
            sellPriceElem.setX(labelX + 46);
            sellPriceElem.setY(sellY);
            sellPriceElem.setWidth(82f);
            sellPriceElem.render(stack, mouseX, mouseY, alpha);
            ItemSetting sel2 = itemElements.get(selectedIndex).getSetting();
            sel2.setSellPercentFromString(sellPriceElem.getInputText());
        }
    }


    @Override
    public void mouseClicked(float mouseX, float mouseY, int button) {
        stringElements.forEach(e -> e.mouseClicked(mouseX, mouseY, button));

        List<ItemElement> filteredItems = getFilteredItems();

        float viewportTop = y + 41f;
        float viewportHeight = getHeight() - 86f;
        boolean inViewport = MathUtil.isHovered(mouseX, mouseY, x, viewportTop, getWidth(), viewportHeight);

        if (inViewport && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            for (ItemElement elem : filteredItems) {
                float elemTop = elem.getY();
                float elemBottom = elemTop + 15f;
                if (!(elemBottom > viewportTop && elemTop < viewportTop + viewportHeight)) continue;

                boolean hoveredItem = MathUtil.isHovered(mouseX, mouseY, elem.getX(), elem.getY(), elem.getWidth(), 15f);
                boolean hoveredCheckbox = MathUtil.isHovered(mouseX, mouseY, elem.getX() + 96, elem.getY() + 3, 9, 9);
                if (hoveredItem && !hoveredCheckbox) {
                    if (!elem.isSelected()) {
                        for (ItemElement e : itemElements) e.setSelected(false);
                        elem.setSelected(true);
                        selectedIndex = itemElements.indexOf(elem);
                    }
                    return;
                }
            }
        }

        if (inViewport) {
            for (ItemElement elem : filteredItems) {
                float elemTop = elem.getY();
                float elemBottom = elemTop + 15f;
                if (elemBottom > viewportTop && elemTop < viewportTop + viewportHeight) {
                    elem.mouseClicked(mouseX, mouseY, button);
                }
            }
        }

        if (selectedIndex != -1) {
            sellToggleElements.get(selectedIndex).mouseClicked(mouseX, mouseY, button);
            sellPriceElements.get(selectedIndex).mouseClicked(mouseX, mouseY, button);
        }

        if (selectedIndex != -1) priceElements.get(selectedIndex).mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float viewportTop = y + 41f;
        float viewportHeight = getHeight() - 86f;
        if (MathUtil.isHovered((float) mouseX, (float) mouseY, x, viewportTop, getWidth(), viewportHeight)) {
            float prev = listScroll;
            listScroll += (float) (delta * 10);
            if (listMaxHeight > viewportHeight) {
                listScroll = MathHelper.clamp(listScroll, -listMaxHeight + viewportHeight, 0);
            } else {
                listScroll = 0;
            }
            return prev != listScroll;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }


    private List<ItemElement> getFilteredItems() {
        String filter = stringElements.get(0).getInputText().trim();
        List<ItemElement> filtered = new ArrayList<>();
        if (filter.equalsIgnoreCase("!вкл")) {
            for (ItemElement elem : itemElements) if (elem.getSetting().get()) filtered.add(elem);
        } else if (filter.equalsIgnoreCase("!выкл")) {
            for (ItemElement elem : itemElements) if (!elem.getSetting().get()) filtered.add(elem);
        } else if (!filter.isEmpty()) {
            for (ItemElement elem : itemElements) {
                String name = elem.getSetting().getName();
                if (name != null && name.toLowerCase().contains(filter.toLowerCase())) {
                    filtered.add(elem);
                }
            }
        } else {
            filtered.addAll(itemElements);
        }
        return filtered;
    }


    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        stringElements.forEach(e -> e.keyPressed(keyCode, scanCode, modifiers));
        if (selectedIndex != -1) {
            boolean ctrlV = keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            StringElement priceElem = priceElements.get(selectedIndex);
            StringElement sellElem = sellPriceElements.get(selectedIndex);

            if (ctrlV) {
                String clip = GLFW.glfwGetClipboardString(GLFW.glfwGetCurrentContext());
                if (clip != null && !clip.isEmpty()) {
                    StringBuilder digits = new StringBuilder();
                    for (int i = 0; i < clip.length(); i++) {
                        char ch = clip.charAt(i);
                        if (Character.isDigit(ch)) digits.append(ch);
                    }
                    String toPaste = digits.toString();

                    if (priceElem.isFocused) {
                        String current = priceElem.getInputText();
                        if (current == null) current = "";
                        if (current.isEmpty()) {
                            int idx = 0;
                            while (idx < toPaste.length() && toPaste.charAt(idx) == '0') idx++;
                            toPaste = toPaste.substring(idx);
                        }
                        int capacity = Math.max(0, 8 - NumberUtil.countDigits(current));
                        int limit = Math.min(capacity, toPaste.length());
                        for (int i = 0; i < limit; i++) {
                            priceElem.charTyped(toPaste.charAt(i), 0);
                        }
                        String formatted = NumberUtil.formatThousands(priceElem.getInputText());
                        priceElem.setInputText(formatted);
                    } else if (sellElem.isFocused) {
                        String current = sellElem.getInputText();
                        if (current == null) current = "";
                        if (current.isEmpty()) {
                            int idx = 0;
                            while (idx < toPaste.length() && toPaste.charAt(idx) == '0') idx++;
                            toPaste = toPaste.substring(idx);
                        }
                        int capacity = Math.max(0, 4 - current.length());
                        int limit = Math.min(capacity, toPaste.length());
                        for (int i = 0; i < limit; i++) {
                            sellElem.charTyped(toPaste.charAt(i), 0);
                        }
                    }
                }
                return;
            }

            priceElem.keyPressed(keyCode, scanCode, modifiers);
            if (priceElem.isFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                String formatted = NumberUtil.formatThousands(priceElem.getInputText());
                priceElem.setInputText(formatted);
            }
            sellElem.keyPressed(keyCode, scanCode, modifiers);
        }
        super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public void charTyped(char codePoint, int modifiers) {
        stringElements.forEach(e -> e.charTyped(codePoint, modifiers));
        if (selectedIndex != -1) {
            StringElement priceElem = priceElements.get(selectedIndex);
            StringElement sellElem = sellPriceElements.get(selectedIndex);

            if (Character.isDigit(codePoint)) {
                if (priceElem.isFocused) {
                    String current = priceElem.getInputText();
                    if (current == null) current = "";
                    if (NumberUtil.countDigits(current) < 8 && !(current.isEmpty() && codePoint == '0')) {
                        priceElem.charTyped(codePoint, modifiers);
                        String formatted = NumberUtil.formatThousands(priceElem.getInputText());
                        priceElem.setInputText(formatted);
                    }
                } else if (sellElem.isFocused) {
                    String current = sellElem.getInputText();
                    if (current == null) current = "";
                    if (current.length() < 4 && !(current.isEmpty() && codePoint == '0')) {
                        sellElem.charTyped(codePoint, modifiers);
                    }
                }
            }
        }
        super.charTyped(codePoint, modifiers);
    }
}