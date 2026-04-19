package mods.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class VoiceChatScreenBase extends Screen {

    public static final int FONT_COLOR = 4210752;

    protected List<HoverArea> hoverAreas;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected VoiceChatScreenBase(ITextComponent title, int xSize, int ySize) {
        super(title);
        this.xSize = xSize;
        this.ySize = ySize;
        this.hoverAreas = new ArrayList<>();
    }

    @Override
    protected void init() {
        buttons.clear();
        children.clear();
        super.init();

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        renderBackground(poseStack, mouseX, mouseY, delta);
        super.render(poseStack, mouseX, mouseY, delta);
        renderForeground(poseStack, mouseX, mouseY, delta);
    }

    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {

    }

    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {

    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    protected boolean isIngame() {
        return minecraft.world != null;
    }

    protected int getFontColor() {
        return isIngame() ? FONT_COLOR : TextFormatting.WHITE.getColor();
    }

    public void drawHoverAreas(MatrixStack matrixStack, int mouseX, int mouseY) {
        for (HoverArea hoverArea : hoverAreas) {
            if (hoverArea.tooltip != null && hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                renderTooltip(matrixStack, hoverArea.tooltip.get(), mouseX - guiLeft, mouseY - guiTop);
            }
        }
    }

    public record HoverArea(int posX, int posY, int width, int height,
                            @Nullable Supplier<List<IReorderingProcessor>> tooltip) {
        public HoverArea(int posX, int posY, int width, int height) {
            this(posX, posY, width, height, null);
        }

        public HoverArea(int posX, int posY, int width, int height, Supplier<List<IReorderingProcessor>> tooltip) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.tooltip = tooltip;
        }

        public boolean isHovered(int guiLeft, int guiTop, int mouseX, int mouseY) {
            if (mouseX >= guiLeft + posX && mouseX < guiLeft + posX + width) {
                return mouseY >= guiTop + posY && mouseY < guiTop + posY + height;
            }
            return false;
        }
    }

}
