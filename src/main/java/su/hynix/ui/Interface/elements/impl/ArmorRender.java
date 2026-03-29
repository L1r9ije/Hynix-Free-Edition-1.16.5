package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import su.hynix.events.EventRender2D;
import su.hynix.ui.Interface.elements.ElementRender;

@RequiredArgsConstructor
public class ArmorRender implements ElementRender {

    @Override
    public void render(EventRender2D.Post event) {
        boolean hasArmor = false;
        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) {
                hasArmor = true;
                break;
            }
        }

        if (!hasArmor) return;

        mc.gameRenderer.setupOverlayRendering();
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/widgets.png"));
        MatrixStack matrix = event.getStack();
        int screenWidth = mc.getMainWindow().getScaledWidth();
        int screenHeight = mc.getMainWindow().getScaledHeight();
        int centerX = screenWidth / 2;

        boolean isRightHand = mc.player.getPrimaryHand().opposite() == HandSide.RIGHT && !mc.player.getHeldItemOffhand().isEmpty();
        int handOffset = isRightHand ? 29 : 0;

        RenderSystem.clearCurrentColor();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.defaultAlphaFunc();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        int xPos1 = centerX + 91 + 10 + handOffset;
        int xPos2 = centerX + 141 + 1 + handOffset;
        int yPos = screenHeight - 22;

        AbstractGui.blit(matrix, xPos1, yPos, 0, 0, 0, 41, 22);
        AbstractGui.blit(matrix, xPos2, yPos, 0, 141, 0, 41, 22);

        int xOffset = 0;
        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            mc.ingameGUI.renderHotbarItem(xPos1 + 3 + xOffset, yPos + 3, event.getPartialTicks(), mc.player, itemStack);
            xOffset += 20;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.clearCurrentColor();
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.gameRenderer.setupOverlayRendering(2);
    }
}