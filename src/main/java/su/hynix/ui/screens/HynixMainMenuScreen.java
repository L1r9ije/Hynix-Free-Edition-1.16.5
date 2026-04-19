package su.hynix.ui.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static su.hynix.utils.Wrapper.mc;

public class HynixMainMenuScreen extends Screen {
    private static final float CARD_WIDTH = 126f;
    private static final float CARD_HEIGHT = 148f;
    private static final float CARD_GAP = 11f;
    private static final float BTN_HEIGHT = 18f;
    private final Random random = new Random();
    private final List<Snowflake> snowflakes = new ArrayList<>();
    private String fastConnectInput = "mc.hynix.su";
    private boolean fastConnectFocused;
    private long fastConnectBlink;

    public HynixMainMenuScreen() {
        super(new StringTextComponent("Main menu"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(mouseX, mouseY);
        renderSnow(matrixStack);
        float centerX = mc.getMainWindow().getScaledWidth() / 2f;
        float baseY = mc.getMainWindow().getScaledHeight() / 2f - 53f;
        float firstCardX = centerX - (CARD_WIDTH * 3 + CARD_GAP * 2) / 2f;

        drawTitle(matrixStack, centerX, baseY - 56f);
        renderFastConnectCard(matrixStack, firstCardX, baseY, mouseX, mouseY);
        renderMainMenuCard(matrixStack, firstCardX + CARD_WIDTH + CARD_GAP, baseY, mouseX, mouseY);
        renderAccountCard(matrixStack, firstCardX + (CARD_WIDTH + CARD_GAP) * 2f, baseY, mouseX, mouseY);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void drawTitle(MatrixStack matrixStack, float centerX, float y) {
        String title = "Hynix Client";
        Fonts.sf_bold[37].drawString(matrixStack, title, centerX - Fonts.sf_bold[37].getWidth(title) / 2f, y, -1);
    }

    private void renderBackground(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        int renderWidth = (int) (windowWidth * 1.06f);
        int renderHeight = (int) (windowHeight * 1.06f);

        float normMouseX = (mouseX / (float) windowWidth) * 2f - 1f;
        float normMouseY = (mouseY / (float) windowHeight) * 2f - 1f;
        float maxOffsetX = (renderWidth - windowWidth) / 2.0f;
        float maxOffsetY = (renderHeight - windowHeight) / 2.0f;
        float offsetX = MathHelper.clamp(normMouseX * 2.5f, -maxOffsetX, maxOffsetX);
        float offsetY = MathHelper.clamp(normMouseY * 2.5f, -maxOffsetY, maxOffsetY);

        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/mainmenu/background.png"), offsetX - (renderWidth - windowWidth) / 2f, offsetY - (renderHeight - windowHeight) / 2f, renderWidth, renderHeight, -1);
        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/mainmenu/black_background.png"), offsetX - (renderWidth - windowWidth) / 2f, offsetY - (renderHeight - windowHeight) / 2f, renderWidth, renderHeight, ColorUtil.getColor(255, 255, 255, 165));
        RenderUtil.drawImage2D(new ResourceLocation("hynix/icons/mainmenu/blue_background.png"), offsetX - (renderWidth - windowWidth) / 2f, offsetY - (renderHeight - windowHeight) / 2f, renderWidth, renderHeight, ColorUtil.getColor(255, 255, 255, 210));
    }

    private void renderFastConnectCard(MatrixStack matrixStack, float x, float y, int mouseX, int mouseY) {
        drawCard(matrixStack, x, y, "FastConnect");
        Fonts.sf_regular[12].drawString(matrixStack, "Enter the IP address in the field below", x + 10f, y + 28f, ColorUtil.getColor(220, 220, 220, 105));
        drawIpInput(matrixStack, x + 10f, y + 38f, CARD_WIDTH - 20f, 22f, mouseX, mouseY);
        drawActionButton(matrixStack, x + 10f, y + 64f, CARD_WIDTH - 20f, BTN_HEIGHT, "Connect", hovered(mouseX, mouseY, x + 10f, y + 64f, CARD_WIDTH - 20f, BTN_HEIGHT));
        Fonts.sf_semibold[17].drawString(matrixStack, "Recent servers", x + 10f, y + 87f, ColorUtil.getColor(230, 230, 230, 190));
        Fonts.sf_regular[12].drawString(matrixStack, "Recent servers that you visited", x + 10f, y + 99f, ColorUtil.getColor(220, 220, 220, 105));
        List<RecentEntry> recent = getRecentServers();
        drawRecentServerLine(matrixStack, x + 10f, y + 108f, CARD_WIDTH - 20f, recent.get(0));
        drawRecentServerLine(matrixStack, x + 10f, y + 128f, CARD_WIDTH - 20f, recent.get(1));
    }

    private void renderMainMenuCard(MatrixStack matrixStack, float x, float y, int mouseX, int mouseY) {
        drawCard(matrixStack, x, y, "Main Menu");
        drawActionButton(matrixStack, x + 10f, y + 34f, CARD_WIDTH - 20f, BTN_HEIGHT, "Одиночная игра", hovered(mouseX, mouseY, x + 10f, y + 34f, CARD_WIDTH - 20f, BTN_HEIGHT));
        drawActionButton(matrixStack, x + 10f, y + 56f, CARD_WIDTH - 20f, BTN_HEIGHT, "Сетевая игра", hovered(mouseX, mouseY, x + 10f, y + 56f, CARD_WIDTH - 20f, BTN_HEIGHT));
        drawActionButton(matrixStack, x + 10f, y + 78f, CARD_WIDTH - 20f, BTN_HEIGHT, "Настройки", hovered(mouseX, mouseY, x + 10f, y + 78f, CARD_WIDTH - 20f, BTN_HEIGHT));
        drawActionButton(matrixStack, x + 10f, y + 100f, CARD_WIDTH - 20f, BTN_HEIGHT, "Выход", hovered(mouseX, mouseY, x + 10f, y + 100f, CARD_WIDTH - 20f, BTN_HEIGHT));
    }

    private void renderAccountCard(MatrixStack matrixStack, float x, float y, int mouseX, int mouseY) {
        drawCard(matrixStack, x, y, "Account manager");
        Fonts.sf_regular[15].drawString(matrixStack, "Account list", x + 10f, y + 36f, ColorUtil.getColor(230, 230, 230, 165));
        drawSelectLine(matrixStack, x + 10f, y + 52f, CARD_WIDTH - 20f, mc.session != null ? mc.session.getUsername() : "Player");
        drawActionButton(matrixStack, x + 10f, y + 80f, CARD_WIDTH - 20f, BTN_HEIGHT + 2f, "Open accounts", hovered(mouseX, mouseY, x + 10f, y + 80f, CARD_WIDTH - 20f, BTN_HEIGHT + 2f));
    }

    private void drawCard(MatrixStack matrixStack, float x, float y, String title) {
        int c1 = ColorUtil.getColor(14, 14, 14, 220);
        int c2 = ColorUtil.getColor(9, 9, 9, 220);
        int c3 = ColorUtil.getColor(21, 21, 21, 220);
        int c4 = ColorUtil.getColor(10, 10, 10, 220);
        RenderUtil.drawRoundedRectangleGradient(x, y, CARD_WIDTH, CARD_HEIGHT, 7f, c1, c2, c3, c4, 1);
        RenderUtil.drawRoundedRectangle(x + 1f, y + 1f, CARD_WIDTH - 2f, CARD_HEIGHT - 2f, 6f, ColorUtil.getColor(0, 0, 0, 35));
        Fonts.sf_semibold[16].drawString(matrixStack, title, x + 9f, y + 12f, -1);
    }

    private void drawSelectLine(MatrixStack matrixStack, float x, float y, float width, String text) {
        RenderUtil.drawRoundedRectangle(x, y, width, 18f, 4f, ColorUtil.getColor(16, 16, 16, 205));
        RenderUtil.drawRoundedRectangle(x + width - 17f, y + 5f, 8f, 8f, 2.5f, ColorUtil.getColor(29, 29, 29, 225));
        Fonts.sf_regular[13].drawString(matrixStack, text, x + 6f, y + 6f, ColorUtil.getColor(220, 220, 220, 185));
    }

    private void drawIpInput(MatrixStack matrixStack, float x, float y, float width, float height, int mouseX, int mouseY) {
        boolean isHovered = hovered(mouseX, mouseY, x, y, width, height);
        int border = fastConnectFocused ? ColorUtil.getColor(72, 145, 233, 190) : ColorUtil.getColor(110, 110, 110, 80);
        RenderUtil.drawRoundedRectangle(x, y, width, height, 4f, ColorUtil.getColor(18, 18, 18, 220));
        RenderUtil.drawRoundedRectangle(x + 0.8f, y + 0.8f, width - 1.6f, height - 1.6f, 3.6f, ColorUtil.getColor(12, 12, 12, isHovered ? 205 : 175));
        RenderUtil.drawRoundedRectangle(x, y, width, 1.1f, 1f, border);

        String text = fastConnectInput.isEmpty() ? "mc.funtime.su" : fastConnectInput;
        int textColor = fastConnectInput.isEmpty() ? ColorUtil.getColor(180, 180, 180, 100) : ColorUtil.getColor(235, 235, 235, 220);
        Fonts.sf_regular[14].drawString(matrixStack, text, x + 6f, y + 8f, textColor);
        if (fastConnectFocused && (System.currentTimeMillis() - fastConnectBlink) % 1000L < 500L) {
            float textWidth = Fonts.sf_regular[14].getWidth(fastConnectInput);
            RenderUtil.drawRoundedRectangle(x + 7f + textWidth, y + 5f, 1.1f, height - 10f, 0.5f, -1);
        }
    }

    private void drawRecentServerLine(MatrixStack matrixStack, float x, float y, float width, RecentEntry entry) {
        RenderUtil.drawRoundedRectangle(x, y, width, 18f, 4f, ColorUtil.getColor(17, 17, 17, 220));
        RenderUtil.drawRoundedRectangle(x + 4f, y + 3.5f, 11f, 11f, 2.2f, ColorUtil.getColor(84, 116, 46, 225));
        Fonts.sf_regular[13].drawString(matrixStack, entry.label, x + 19f, y + 6f, ColorUtil.getColor(220, 220, 220, 205));
        Fonts.sf_regular[13].drawString(matrixStack, "x", x + width - 10f, y + 5.5f, ColorUtil.getColor(215, 215, 215, 170));
    }

    private void drawActionButton(MatrixStack matrixStack, float x, float y, float width, float height, String text, boolean hovered) {
        int bg = hovered ? ColorUtil.getColor(46, 132, 231, 205) : ColorUtil.getColor(20, 20, 20, 205);
        RenderUtil.drawRoundedRectangle(x, y, width, height, 4f, bg);
        int textColor = hovered ? -1 : ColorUtil.getColor(232, 232, 232, 210);
        Fonts.sf_regular[14].drawString(matrixStack, text, x + width / 2f - Fonts.sf_regular[14].getWidth(text) / 2f, y + height / 2f - Fonts.sf_regular[14].getHeight() / 2f + 1f, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        float centerX = mc.getMainWindow().getScaledWidth() / 2f;
        float baseY = mc.getMainWindow().getScaledHeight() / 2f - 53f;
        float firstCardX = centerX - (CARD_WIDTH * 3 + CARD_GAP * 2) / 2f;

        if (hovered((int) mouseX, (int) mouseY, firstCardX + 10f, baseY + 34f, 49f, BTN_HEIGHT)
                || hovered((int) mouseX, (int) mouseY, firstCardX + CARD_WIDTH + CARD_GAP + 10f, baseY + 34f, CARD_WIDTH - 20f, BTN_HEIGHT)) {
            openSingleplayerSafe();
            return true;
        }

        if (hovered((int) mouseX, (int) mouseY, firstCardX + 10f, baseY + 64f, CARD_WIDTH - 20f, BTN_HEIGHT)) {
            connectDirect(fastConnectInput.trim());
            return true;
        }

        if (hovered((int) mouseX, (int) mouseY, firstCardX + CARD_WIDTH + CARD_GAP + 10f, baseY + 56f, CARD_WIDTH - 20f, BTN_HEIGHT)) {
            Screen screen = mc.gameSettings.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
            mc.displayGuiScreen(screen);
            return true;
        }

        if (hovered((int) mouseX, (int) mouseY, firstCardX + 10f, baseY + 38f, CARD_WIDTH - 20f, 22f)) {
            fastConnectFocused = true;
            fastConnectBlink = System.currentTimeMillis();
            return true;
        }
        List<RecentEntry> entries = getRecentServers();
        if (hovered((int) mouseX, (int) mouseY, firstCardX + 10f, baseY + 108f, CARD_WIDTH - 20f, 18f)) {
            fastConnectInput = entries.get(0).ip;
            fastConnectFocused = true;
            connectDirect(fastConnectInput.trim());
            return true;
        }
        if (hovered((int) mouseX, (int) mouseY, firstCardX + 10f, baseY + 128f, CARD_WIDTH - 20f, 18f)) {
            fastConnectInput = entries.get(1).ip;
            fastConnectFocused = true;
            connectDirect(fastConnectInput.trim());
            return true;
        }
        fastConnectFocused = false;

        float menuCardX = firstCardX + CARD_WIDTH + CARD_GAP;
        if (hovered((int) mouseX, (int) mouseY, menuCardX + 10f, baseY + 78f, CARD_WIDTH - 20f, BTN_HEIGHT)) {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
            return true;
        }
        if (hovered((int) mouseX, (int) mouseY, menuCardX + 10f, baseY + 100f, CARD_WIDTH - 20f, BTN_HEIGHT)) {
            mc.shutdown();
            return true;
        }

        float accountCardX = firstCardX + (CARD_WIDTH + CARD_GAP) * 2f;
        if (hovered((int) mouseX, (int) mouseY, accountCardX + 10f, baseY + 80f, CARD_WIDTH - 20f, BTN_HEIGHT + 2f)) {
            mc.displayGuiScreen(new AccountsScreen());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean hovered(int mouseX, int mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void openSingleplayerSafe() {
        try {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        } catch (Throwable throwable) {
            mc.displayGuiScreen(new ConfirmScreen(result -> mc.displayGuiScreen(this),
                    new StringTextComponent("Singleplayer временно недоступен"),
                    new TranslationTextComponent("menu.multiplayer")));
        }
    }

    private List<RecentEntry> getRecentServers() {
        List<RecentEntry> out = new ArrayList<>();
        try {
            ServerList serverList = new ServerList(mc);
            serverList.loadServerList();
            for (int i = 0; i < Math.min(2, serverList.countServers()); i++) {
                ServerData data = serverList.getServerData(i);
                String ip = data.serverIP == null || data.serverIP.isEmpty() ? "unknown.server" : data.serverIP;
                String label = data.serverName == null || data.serverName.isEmpty() ? ip : data.serverName;
                out.add(new RecentEntry(label, ip));
            }
        } catch (Exception ignored) {
        }
        while (out.size() < 2) {
            out.add(new RecentEntry("No recent server", ""));
        }
        return out;
    }

    @Override
    protected void init() {
        if (snowflakes.isEmpty() || this.width != mc.getMainWindow().getScaledWidth() || this.height != mc.getMainWindow().getScaledHeight()) {
            snowflakes.clear();
            for (int i = 0; i < 70; i++) {
                snowflakes.add(new Snowflake(random.nextInt(Math.max(this.width, 1)), random.nextInt(Math.max(this.height, 1))));
            }
        }
    }

    private void renderSnow(MatrixStack matrixStack) {
        for (Snowflake snowflake : snowflakes) {
            snowflake.update(this.width, this.height);
            RenderUtil.drawRoundedRectangle(snowflake.x, snowflake.y, snowflake.size, snowflake.size, snowflake.size / 2f, ColorUtil.getColor(255, 255, 255, 220));
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (fastConnectFocused && Character.toString(codePoint).matches("[a-zA-Z0-9_\\-\\.:]")) {
            if (fastConnectInput.length() < 48) {
                fastConnectInput += codePoint;
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fastConnectFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !fastConnectInput.isEmpty()) {
                fastConnectInput = fastConnectInput.substring(0, fastConnectInput.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                connectDirect(fastConnectInput.trim());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                String value = mc.keyboardListener.getClipboardString();
                if (value != null && !value.isEmpty()) {
                    value = value.replaceAll("[^a-zA-Z0-9_\\-\\.:]", "");
                    fastConnectInput = (fastConnectInput + value);
                    if (fastConnectInput.length() > 48) {
                        fastConnectInput = fastConnectInput.substring(0, 48);
                    }
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                fastConnectFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void connectDirect(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        ServerData data = new ServerData("FastConnect", ip, false);
        mc.gameSettings.lastServer = ip;
        mc.displayGuiScreen(new ConnectingScreen(this, mc, data));
    }

    private record RecentEntry(String label, String ip) {
    }

    private class Snowflake {
        float x;
        float y;
        float speed;
        float drift;
        float size;

        Snowflake(float x, float y) {
            this.x = x;
            this.y = y;
            this.speed = 0.35f + random.nextFloat() * 0.95f;
            this.drift = -0.35f + random.nextFloat() * 0.7f;
            this.size = 1.2f + random.nextFloat() * 2.4f;
        }

        void update(int width, int height) {
            y += speed;
            x += drift;
            if (y > height + 4) {
                y = -4;
                x = random.nextInt(Math.max(width, 1));
            }
            if (x < -4) x = width + 4;
            if (x > width + 4) x = -4;
        }
    }
}
