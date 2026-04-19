package su.hynix.ui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import su.hynix.managers.impl.AccountManager;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.ScissorUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.*;

import static su.hynix.utils.Wrapper.mc;

public class AccountsScreen extends Screen {
    private final AccountManager accountManager;
    private final Map<String, Long> accountNames = new LinkedHashMap<>();
    private final Map<String, ResourceLocation> skinCache = new LinkedHashMap<>();
    private String selectedAccount;

    private String inputText = "";
    private boolean typing;
    private float scroll;
    private float animatedScroll;
    private float maxScroll;
    private long blinkStart;

    public AccountsScreen() {
        super(new StringTextComponent("Alt Manager"));
        this.accountManager = new AccountManager();
        loadFromConfig();
    }

    private void loadFromConfig() {
        if (accountManager.getData() == null) {
            accountManager.init();
        }
        accountNames.clear();
        accountNames.putAll(accountManager.getAccountNames());
        selectedAccount = accountManager.getSelectedAccount();
    }

    private void saveAccount(String name) {
        if (name.isEmpty() || accountNames.containsKey(name)) {
            return;
        }
        accountNames.put(name, System.currentTimeMillis());
        accountManager.addAccount(name);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(mouseX, mouseY);

        float panelWidth = 306f;
        float panelHeight = 272f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - panelWidth / 2f;
        float y = mc.getMainWindow().getScaledHeight() / 2f - panelHeight / 2f;

        RenderUtil.drawRoundedRectangleGradient(x, y, panelWidth, panelHeight, 7f,
                ColorUtil.getColor(9, 9, 9, 215), ColorUtil.getColor(8, 8, 8, 215),
                ColorUtil.getColor(24, 24, 24, 215), ColorUtil.getColor(12, 12, 12, 215), 1);

        Fonts.sf_bold[24].drawString(matrixStack, "Alt Manager", x + panelWidth / 2f - Fonts.sf_bold[24].getWidth("Alt Manager") / 2f, y + 10f, -1);
        String currentNick = "Current nick: " + (mc.session != null ? mc.session.getUsername() : "Player");
        Fonts.sf_regular[15].drawString(matrixStack, currentNick, x + panelWidth / 2f - Fonts.sf_regular[15].getWidth(currentNick) / 2f, y + 34f, ColorUtil.getColor(230, 230, 230, 165));

        float listX = x + 10f;
        float listY = y + 56f;
        float listW = panelWidth - 20f;
        float listH = 164f;
        RenderUtil.drawRoundedRectangle(listX, listY, listW, listH, 4f, ColorUtil.getColor(10, 10, 10, 125));

        List<String> names = new ArrayList<>(accountNames.keySet());
        maxScroll = Math.max(0f, names.size() * 23f - (listH - 6f));
        scroll = MathHelper.clamp(scroll, -maxScroll, 0f);
        animatedScroll = MathHelper.lerp(0.3f, animatedScroll, scroll);

        ScissorUtil.start(listX, listY, listW, listH);
        float drawY = listY + 4f + animatedScroll;
        for (String name : names) {
            boolean isCurrent = name.equals(selectedAccount);
            int bg = isCurrent ? ColorUtil.getColor(35, 116, 220, 110) : ColorUtil.getColor(18, 18, 18, 140);
            RenderUtil.drawRoundedRectangle(listX + 3f, drawY, listW - 6f, 20f, 3f, bg);
            RenderUtil.drawRoundedHead(getSkin(name), null, listX + 6f, drawY + 2f, 16f, 16f, 3f, 1f);
            Fonts.sf_medium[16].drawString(matrixStack, name, listX + 26f, drawY + 7f, -1);
            drawY += 23f;
        }
        ScissorUtil.end();

        float inputY = y + panelHeight - 44f;
        RenderUtil.drawRoundedRectangle(listX, inputY, listW, 18f, 3.5f, ColorUtil.getColor(14, 14, 14, 170));
        String placeholder = "Type nick and press Enter";
        String textToDraw = inputText.isEmpty() && !typing ? placeholder : inputText;
        int color = inputText.isEmpty() && !typing ? ColorUtil.getColor(200, 200, 200, 95) : -1;
        Fonts.sf_regular[14].drawString(matrixStack, textToDraw, listX + 5f, inputY + 6f, color);
        if (typing && showCursor()) {
            float textWidth = Fonts.sf_regular[14].getWidth(inputText);
            RenderUtil.drawRoundedRectangle(listX + 6f + textWidth, inputY + 4f, 1.2f, 10f, 0.4f, -1);
        }

        float randomX = x + panelWidth - 82f;
        float randomY = y + panelHeight - 21f;
        boolean randomHovered = hovered(mouseX, mouseY, randomX, randomY, 72f, 14f);
        RenderUtil.drawRoundedRectangle(randomX, randomY, 72f, 14f, 3f, randomHovered ? ColorUtil.getColor(38, 122, 227, 210) : ColorUtil.getColor(17, 17, 17, 180));
        Fonts.sf_regular[14].drawString(matrixStack, "Random", randomX + 36f - Fonts.sf_regular[14].getWidth("Random") / 2f, randomY + 4f, -1);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void renderBackground(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        int renderWidth = (int) (windowWidth * 1.05f);
        int renderHeight = (int) (windowHeight * 1.05f);
        float normMouseX = (mouseX / (float) windowWidth) * 2f - 1f;
        float normMouseY = (mouseY / (float) windowHeight) * 2f - 1f;
        float maxOffsetX = (renderWidth - windowWidth) / 2.0f;
        float maxOffsetY = (renderHeight - windowHeight) / 2.0f;
        float offsetX = MathHelper.clamp(normMouseX * 2.5f, -maxOffsetX, maxOffsetX);
        float offsetY = MathHelper.clamp(normMouseY * 2.5f, -maxOffsetY, maxOffsetY);

        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/mainmenu/background.png"), offsetX - (renderWidth - windowWidth) / 2f, offsetY - (renderHeight - windowHeight) / 2f, renderWidth, renderHeight, -1);
        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/mainmenu/black_background.png"), offsetX - (renderWidth - windowWidth) / 2f, offsetY - (renderHeight - windowHeight) / 2f, renderWidth, renderHeight, ColorUtil.getColor(255, 255, 255, 180));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float panelWidth = 306f;
        float panelHeight = 272f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - panelWidth / 2f;
        float y = mc.getMainWindow().getScaledHeight() / 2f - panelHeight / 2f;
        float listX = x + 10f;
        float listY = y + 56f;
        float listW = panelWidth - 20f;
        float listH = 164f;
        float inputY = y + panelHeight - 44f;
        float randomX = x + panelWidth - 82f;
        float randomY = y + panelHeight - 21f;

        if (button == 0 && hovered((int) mouseX, (int) mouseY, listX, inputY, listW, 18f)) {
            typing = true;
            blinkStart = System.currentTimeMillis();
            return true;
        }

        if (button == 0 && hovered((int) mouseX, (int) mouseY, randomX, randomY, 72f, 14f)) {
            saveAccount(generateRandomName());
            return true;
        }

        if (hovered((int) mouseX, (int) mouseY, listX, listY, listW, listH)) {
            List<String> names = new ArrayList<>(accountNames.keySet());
            float drawY = listY + 4f + animatedScroll;
            Iterator<String> iterator = names.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                if (hovered((int) mouseX, (int) mouseY, listX + 3f, drawY, listW - 6f, 20f)) {
                    if (button == 0) {
                        selectedAccount = name;
                        accountManager.selectAccount(name);
                        mc.session.setUsername(name);
                    } else if (button == 1) {
                        accountNames.remove(name);
                        accountManager.removeAccount(name);
                        skinCache.remove(name);
                    }
                    return true;
                }
                drawY += 23f;
            }
        }

        if (button == 0) {
            typing = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float panelWidth = 306f;
        float panelHeight = 272f;
        float x = mc.getMainWindow().getScaledWidth() / 2f - panelWidth / 2f;
        float y = mc.getMainWindow().getScaledHeight() / 2f - panelHeight / 2f;
        float listX = x + 10f;
        float listY = y + 56f;
        float listW = panelWidth - 20f;
        float listH = 164f;
        if (hovered((int) mouseX, (int) mouseY, listX, listY, listW, listH)) {
            scroll += (float) delta * 13f;
            scroll = MathHelper.clamp(scroll, -maxScroll, 0f);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typing && Character.toString(codePoint).matches("[a-zA-Z0-9_]") && inputText.length() < 20) {
            inputText += codePoint;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputText.isEmpty()) {
                inputText = inputText.substring(0, inputText.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                String value = inputText.trim();
                if (value.length() >= 3) {
                    saveAccount(value);
                }
                inputText = "";
                typing = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                String paste = mc.keyboardListener.getClipboardString();
                if (!paste.isEmpty()) {
                    paste = paste.replaceAll("[^a-zA-Z0-9_]", "");
                    inputText = (inputText + paste);
                    if (inputText.length() > 20) {
                        inputText = inputText.substring(0, 20);
                    }
                }
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            mc.displayGuiScreen(new HynixMainMenuScreen());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean showCursor() {
        if (blinkStart == 0L) {
            blinkStart = System.currentTimeMillis();
        }
        return (System.currentTimeMillis() - blinkStart) % 1000L < 500L;
    }

    private boolean hovered(int mouseX, int mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private String generateRandomName() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder("Hynix");
        for (int i = 0; i < 5; i++) {
            int idx = (int) (Math.random() * chars.length());
            builder.append(chars.charAt(idx));
        }
        return builder.toString();
    }

    private ResourceLocation getSkin(String name) {
        ResourceLocation cached = skinCache.get(name);
        if (cached != null) {
            return cached;
        }
        ResourceLocation location;
        try {
            location = AbstractClientPlayerEntity.getLocationSkin(name);
            AbstractClientPlayerEntity.getDownloadImageSkin(location, name);
        } catch (Exception ex) {
            location = AbstractClientPlayerEntity.getLocationSkin(String.valueOf(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes())));
        }
        skinCache.put(name, location);
        return location;
    }
}
