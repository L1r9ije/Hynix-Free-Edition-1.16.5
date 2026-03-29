package su.hynix.utils.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;
import su.hynix.utils.Wrapper;
import su.hynix.utils.render.shader.KawaseBlur;
import su.hynix.utils.render.shader.ShaderUtil;

public class RenderUtil implements Wrapper {

    private static boolean image3DBatchActive = false;
    private static boolean rectBatchActive = false;
    private static boolean rectBatchWithColor = false;
    private static boolean rectBatchTextured = false;
    private static ResourceLocation currentBatchTexture = null;
    private static int batchQuadCount = 0;

    public static void drawRoundedRectangle(float x, float y, float width, float height, float radius, int color) {
        drawRoundedRectangle(x, y, width, height, new Vector4f(radius, radius, radius, radius), color);
    }

    public static void drawRound(float x, float y, float width, float height, float radius, int color) {
        drawRound(x, y, width, height, color);
    }

    public static void drawRoundOutline(float x, float y, float width, float height, float radius, float borderSize, int color, float alpha) {
        drawOutlineRound(x, y, width, height, radius, borderSize, color, alpha / 255f);
    }

    public static void drawRoundedRectangleGradient(float x, float y, float width, float height, float radius, int color0, int color1, int color2, int color3, float alpha) {
        drawRoundedRectangleGradient(x, y, width, height, new Vector4f(radius, radius, radius, radius), color0, color1, color2, color3, alpha);
    }

    public static void drawRoundedRectangleGradientGlowed(float x, float y, float width, float height, float radius, int color0, int color1, int color2, int color3, float alpha, float glowRadius) {
        drawRoundedRectangleGradientGlowed(x, y, width, height, new Vector4f(radius, radius, radius, radius), color0, color1, color2, color3, alpha, glowRadius);
    }

    public static void drawBlurredRoundedRectangle(float x, float y, float width, float height, float radius, int color, float alpha) {
        drawBlurredRoundedRectangle(x, y, width, height, new Vector4f(radius, radius, radius, radius), color, alpha);
    }

    public static void drawOutlineRectangle(float x, float y, float width, float height, float radius, int color, float alpha) {
        drawRoundedOutline(x, y, width, height, radius, 0.25f, color, ColorUtil.getColor(255, 255, 255), ColorUtil.getColor(255, 255, 255), color, alpha / 255f, 0, 0, alpha / 255f);
    }

    public static void drawOutlineRectangleBold(float x, float y, float width, float height, float radius, int color, float alpha) {
        drawRoundedOutline(x, y, width, height, radius, 0.75f, color, color, color, color, alpha / 255f, alpha / 255f, alpha / 255f, alpha / 255f);
    }

    public static void beginRectBatch(boolean withColor, boolean textured) {
        if (rectBatchActive) {
            endRectBatch();
        }
        rectBatchActive = true;
        rectBatchWithColor = withColor;
        rectBatchTextured = textured;

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01f);
        if (rectBatchTextured) {
            RenderSystem.enableTexture();
        } else {
            RenderSystem.disableTexture();
        }

        BUFFER.begin(GL11.GL_QUADS, withColor ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_TEX);
    }

    public static void endRectBatch() {
        if (!rectBatchActive) return;
        TESSELLATOR.draw();
        if (!rectBatchTextured) {
            RenderSystem.enableTexture();
        }
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        rectBatchActive = false;
    }

    public static void drawImage3DQuadInternal(ResourceLocation texture, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int color0, int color1, int color2, int color3, float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3) {
        boolean needNewBatch = !image3DBatchActive || !texture.equals(currentBatchTexture) || batchQuadCount >= 8192;

        if (needNewBatch) {
            if (image3DBatchActive) {
                TESSELLATOR.draw();
            }

            currentBatchTexture = texture;
            image3DBatchActive = true;
            batchQuadCount = 0;

            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01f);

            mc.getTextureManager().bindTexture(texture);
            BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        }

        int r0 = (color0 >> 16) & 0xFF;
        int g0 = (color0 >> 8) & 0xFF;
        int b0 = color0 & 0xFF;
        int a0 = color0 >>> 24;

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = color1 >>> 24;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = color2 >>> 24;

        int r3 = (color3 >> 16) & 0xFF;
        int g3 = (color3 >> 8) & 0xFF;
        int b3 = color3 & 0xFF;
        int a3 = color3 >>> 24;

        BUFFER.pos(x0, y0, z0).tex(u0, v0).color(r0, g0, b0, a0).endVertex();
        BUFFER.pos(x1, y1, z1).tex(u1, v1).color(r1, g1, b1, a1).endVertex();
        BUFFER.pos(x2, y2, z2).tex(u2, v2).color(r2, g2, b2, a2).endVertex();
        BUFFER.pos(x3, y3, z3).tex(u3, v3).color(r3, g3, b3, a3).endVertex();

        batchQuadCount++;
    }

    public static void drawImage3DQuad(ResourceLocation texture, boolean boost, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int color) {
        int boostalpha = boost ? 30 : 0;
        int red = Math.min(255, ((color >> 16) & 0xFF) + boostalpha);
        int green = Math.min(255, ((color >> 8) & 0xFF) + boostalpha);
        int blue = Math.min(255, (color & 0xFF) + boostalpha);
        int alpha = color >>> 24;

        int boostedColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        drawImage3DQuadInternal(texture, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, boostedColor, boostedColor, boostedColor, boostedColor, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f);
    }

    public static void flushImage3DBatch() {
        if (image3DBatchActive) {
            TESSELLATOR.draw();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            RenderSystem.shadeModel(7424);
            RenderSystem.depthMask(true);
            image3DBatchActive = false;
            currentBatchTexture = null;
            batchQuadCount = 0;
        }
    }

    public static void drawImage2D(ResourceLocation image, float x, float y, float width, float height, int color) {
        mc.getTextureManager().bindTexture(image);
        int filter;
        if (width > 128 || height > 128) {
            filter = GL11.GL_NEAREST;
        } else {
            filter = GL11.GL_LINEAR;
        }
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = (color >> 24 & 255) / 255.0F;
        RenderSystem.color4f(r, g, b, a);
        beginRectBatch(true, true);
        drawQuads(x, y, width, height, color);
        endRectBatch();
    }

    public static void drawImage3D(ResourceLocation image, float x, float y, float z, float width, float height, int color, boolean boost) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.01f);

        int boostalpha = boost ? 40 : 0;
        int red = Math.min(255, ((color >> 16) & 0xFF) + boostalpha);
        int green = Math.min(255, ((color >> 8) & 0xFF) + boostalpha);
        int blue = Math.min(255, (color & 0xFF) + boostalpha);
        int alpha = color >>> 24;

        mc.getTextureManager().bindTexture(image);
        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        BUFFER.pos(x, y + height, z).tex(0, 1).color(red, green, blue, alpha).endVertex();
        BUFFER.pos(x + width, y + height, z).tex(1, 1).color(red, green, blue, alpha).endVertex();
        BUFFER.pos(x + width, y, z).tex(1, 0f).color(red, green, blue, alpha).endVertex();
        BUFFER.pos(x, y, z).tex(0, 0f).color(red, green, blue, alpha).endVertex();

        TESSELLATOR.draw();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
    }

    public static void drawRoundedRectangle(float x, float y, float width, float height, Vector4f radius, int color) {
        ShaderUtil.rounded_rectangle.attach();
        ShaderUtil.rounded_rectangle.setUniformf("size", width, height);
        ShaderUtil.rounded_rectangle.setUniform("radius", radius.getX(), radius.getY(), radius.getZ(), radius.getW());
        ShaderUtil.rounded_rectangle.setUniform("color", ColorUtil.getColor(color));

        beginRectBatch(false, false);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.rounded_rectangle.detach();
    }

    public static void drawRound(float x, float y, float width, float height, int color) {
        ShaderUtil.round.attach();
        ShaderUtil.round.setUniformf("size", width, height);
        ShaderUtil.round.setUniform("color", ColorUtil.getColor(color));

        beginRectBatch(false, false);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.round.detach();
    }

    public static void drawOutlineRound(float x, float y, float width, float height, float radius, float borderSize, int color, float alpha) {
        ShaderUtil.rounded_outline.attach();

        ShaderUtil.rounded_outline.setUniformf("u_size", width, height);
        ShaderUtil.rounded_outline.setUniformf("u_radius", radius);
        ShaderUtil.rounded_outline.setUniformf("u_border_size", borderSize);
        ShaderUtil.rounded_outline.setUniformf("u_alpha1", alpha);
        ShaderUtil.rounded_outline.setUniformf("u_alpha2", alpha);
        ShaderUtil.rounded_outline.setUniformf("u_alpha3", alpha);
        ShaderUtil.rounded_outline.setUniformf("u_alpha4", alpha);
        ShaderUtil.rounded_outline.setUniformf("u_color_1", ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1);
        ShaderUtil.rounded_outline.setUniformf("u_color_2", ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1);
        ShaderUtil.rounded_outline.setUniformf("u_color_3", ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1);
        ShaderUtil.rounded_outline.setUniformf("u_color_4", ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1);

        beginRectBatch(false, false);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.rounded_outline.detach();
    }

    public static void drawRoundedRectangleGradient(float x, float y, float width, float height, Vector4f radius, int color0, int color1, int color2, int color3, float alpha) {
        ShaderUtil.rounded_rectangle_gradient.attach();

        ShaderUtil.rounded_rectangle_gradient.setUniformf("size", width, height);
        ShaderUtil.rounded_rectangle_gradient.setUniformf("radius", radius.getX(), radius.getY(), radius.getZ(), radius.getW());
        ShaderUtil.rounded_rectangle_gradient.setUniformf("color0", ((color0 >> 16) & 0xFF) / 255f, ((color0 >> 8) & 0xFF) / 255f, (color0 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient.setUniformf("color1", ((color1 >> 16) & 0xFF) / 255f, ((color1 >> 8) & 0xFF) / 255f, (color1 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient.setUniformf("color2", ((color2 >> 16) & 0xFF) / 255f, ((color2 >> 8) & 0xFF) / 255f, (color2 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient.setUniformf("color3", ((color3 >> 16) & 0xFF) / 255f, ((color3 >> 8) & 0xFF) / 255f, (color3 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient.setUniformf("alpha", alpha);

        beginRectBatch(false, false);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.rounded_rectangle_gradient.detach();
    }

    public static void drawRoundedRectangleGradientGlowed(float x, float y, float width, float height, Vector4f radius, int color0, int color1, int color2, int color3, float alpha, float glowRadius) {
        ShaderUtil.rounded_rectangle_gradient_glowed.attach();

        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("size", width, height);
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("radius", radius.getX(), radius.getY(), radius.getZ(), radius.getW());
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("color0", ((color0 >> 16) & 0xFF) / 255f, ((color0 >> 8) & 0xFF) / 255f, (color0 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("color1", ((color1 >> 16) & 0xFF) / 255f, ((color1 >> 8) & 0xFF) / 255f, (color1 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("color2", ((color2 >> 16) & 0xFF) / 255f, ((color2 >> 8) & 0xFF) / 255f, (color2 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("color3", ((color3 >> 16) & 0xFF) / 255f, ((color3 >> 8) & 0xFF) / 255f, (color3 & 0xFF) / 255f);
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("alpha", alpha);
        ShaderUtil.rounded_rectangle_gradient_glowed.setUniformf("glowRadius", glowRadius);

        beginRectBatch(false, false);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.rounded_rectangle_gradient_glowed.detach();
    }

    public static void drawBlurredRoundedRectangle(float x, float y, float width, float height, Vector4f radius, int color, float alpha) {
        RenderSystem.bindTexture(KawaseBlur.blur.BLURRED.framebufferTexture);

        ShaderUtil.blurred_round_rectangle.attach();

        ShaderUtil.blurred_round_rectangle.setUniformf("resolution", mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight());
        ShaderUtil.blurred_round_rectangle.setUniformf("start", x, y);
        ShaderUtil.blurred_round_rectangle.setUniformf("size", width, height);
        ShaderUtil.blurred_round_rectangle.setUniform("round", radius.getX(), radius.getY(), radius.getZ(), radius.getW());
        ShaderUtil.blurred_round_rectangle.setUniform("alpha", alpha);
        ShaderUtil.blurred_round_rectangle.setUniform("color", ColorUtil.getColor(color));

        beginRectBatch(false, true);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.blurred_round_rectangle.detach();
    }

    public static void drawRoundedHead(ResourceLocation skin, LivingEntity target, float x, float y, float width, float height, float radius, float alpha) {
        float hurt_time = target != null ? target.hurtTime : 0;
        hurt_time = hurt_time > 0 ? Math.min(0.25f, hurt_time / target.maxHurtTime) : 0;

        mc.getTextureManager().bindTexture(skin);

        ShaderUtil.rounded_head_texture.attach();

        ShaderUtil.rounded_head_texture.setUniformf("size", width, height);
        ShaderUtil.rounded_head_texture.setUniformf("radius", radius);
        ShaderUtil.rounded_head_texture.setUniformf("hurt_time", hurt_time);
        ShaderUtil.rounded_head_texture.setUniformf("alpha", alpha);
        ShaderUtil.rounded_head_texture.setUniformf("texXSize", 64);
        ShaderUtil.rounded_head_texture.setUniformf("texYSize", 64);

        beginRectBatch(false, true);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.rounded_head_texture.detach();
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float borderSize, int color0, int color1, int color2, int color3, float alpha1, float alpha2, float alpha3, float alpha4) {
        ShaderUtil.outline.attach();

        ShaderUtil.outline.setUniformf("u_size", width, height);
        ShaderUtil.outline.setUniformf("u_radius", radius);
        ShaderUtil.outline.setUniformf("u_border_size", borderSize);
        ShaderUtil.outline.setUniformf("u_alpha1", alpha1);
        ShaderUtil.outline.setUniformf("u_alpha2", alpha2);
        ShaderUtil.outline.setUniformf("u_alpha3", alpha3);
        ShaderUtil.outline.setUniformf("u_alpha4", alpha4);
        ShaderUtil.outline.setUniformf("u_color_1", ((color0 >> 16) & 0xFF) / 255f, ((color0 >> 8) & 0xFF) / 255f, (color0 & 0xFF) / 255f, 1);
        ShaderUtil.outline.setUniformf("u_color_2", ((color1 >> 16) & 0xFF) / 255f, ((color1 >> 8) & 0xFF) / 255f, (color1 & 0xFF) / 255f, 1);
        ShaderUtil.outline.setUniformf("u_color_3", ((color2 >> 16) & 0xFF) / 255f, ((color2 >> 8) & 0xFF) / 255f, (color2 & 0xFF) / 255f, 1);
        ShaderUtil.outline.setUniformf("u_color_4", ((color3 >> 16) & 0xFF) / 255f, ((color3 >> 8) & 0xFF) / 255f, (color3 & 0xFF) / 255f, 1);

        beginRectBatch(false, false);
        drawQuads(x, y, width, height);
        endRectBatch();

        ShaderUtil.outline.detach();
    }

    public static void drawStack(ItemStack itemStack, float x, float y, float size) {
        if (itemStack.isEmpty()) return;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.scalef(size, size, size);

        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
        mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, itemStack, 0, 0);

        RenderSystem.popMatrix();
    }

    public static void drawPotionLiquid(int color, float x, float y, float width, float height) {
        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        AtlasTexture atlas = (AtlasTexture) mc.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite fluidSprite = atlas.getSprite(new ResourceLocation("minecraft", "item/potion_overlay"));
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(r, g, b, a);
        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        BUFFER.pos(x, y + height, 0).tex(fluidSprite.getMinU(), fluidSprite.getMaxV()).endVertex();
        BUFFER.pos(x + width, y + height, 0).tex(fluidSprite.getMaxU(), fluidSprite.getMaxV()).endVertex();
        BUFFER.pos(x + width, y, 0).tex(fluidSprite.getMaxU(), fluidSprite.getMinV()).endVertex();
        BUFFER.pos(x, y, 0).tex(fluidSprite.getMinU(), fluidSprite.getMinV()).endVertex();
        TESSELLATOR.draw();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    public static void drawMinecraftRectangle(MatrixStack matrixStack, float x, float y, float width, float height, int color) {
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(width, height, 1);
        AbstractGui.fill(matrixStack, 0, 0, 1, 1, color);
        matrixStack.pop();
    }

    public static void drawMinecraftGradientRectangle(MatrixStack matrixStack, float x, float y, float width, float height, int color0, int color1) {
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(width, height, 1);
        AbstractGui.fillGradient(matrixStack, 0, 0, 1, 1, color0, color1);
        matrixStack.pop();
    }

    public static void drawQuads(double x, double y, double width, double height) {
        boolean batching = rectBatchActive && !rectBatchWithColor;
        if (!batching) {
            BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }

        BUFFER.pos(x, y, 0).tex(0, 0).endVertex();
        BUFFER.pos(x, y + height, 0).tex(0, 1).endVertex();
        BUFFER.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        BUFFER.pos(x + width, y, 0).tex(1, 0).endVertex();

        if (!batching) {
            TESSELLATOR.draw();
        }
    }

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 0);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translated(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static void drawQuads(float x, float y, float width, float height, int color) {
        boolean batching = rectBatchActive && rectBatchWithColor;
        if (!batching) {
            BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        }

        BUFFER.pos(x, y, 0).tex(0, 0).color(color).endVertex();
        BUFFER.pos(x, y + height, 0).tex(0, 1).color(color).endVertex();
        BUFFER.pos(x + width, y + height, 0).tex(1, 1).color(color).endVertex();
        BUFFER.pos(x + width, y, 0).tex(1, 0).color(color).endVertex();

        if (!batching) {
            TESSELLATOR.draw();
        }
    }
}