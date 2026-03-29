package su.hynix.handlers.impl;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.events.EventRender3D;
import su.hynix.hynix;
import su.hynix.modules.impl.combat.AttackAura;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;

import static su.hynix.utils.Wrapper.mc;

public class TargetESPHandler {
    private static final AnimationUtil szAnim = new AnimationUtil(1f, 6f, Easings.LINEAR);
    private static final AnimationUtil alpAnim = new AnimationUtil(0f, 6f, Easings.LINEAR);
    private static final double[] ghPh = new double[3];
    private static final AnimationUtil htScAnim = new AnimationUtil(1f, 6f, Easings.LINEAR);
    private static final AnimationUtil htClAnim = new AnimationUtil(0f, 6f, Easings.LINEAR);
    private static final AnimationUtil ghApAnim = new AnimationUtil(0f, 6f, Easings.CUBIC_IN_OUT);
    private static Vector3d lstSqPos;
    private static float lstSqHH;
    private static boolean lstHT;
    private static long sqRotMs;
    private static double sqRotPh;
    private static Entity lstSqEnt;
    private static long ghPhMs;
    private static Vector3d lstGhPos;
    private static Entity lstGhEnt;

    @EventTarget
    private void renderSquare(EventRender3D e) {
        AttackAura a = hynix.getInstance().getModuleManager().attackAura;
        Entity t = (a.isEnabled() && a.getTargetesp().is("Ромб")) ? AttackAura.getTarget() : null;
        ActiveRenderInfo cm = mc.getRenderManager().info;
        Vector3d cp = cm.getProjectedView();
        boolean h = t != null;
        alpAnim.update(h ? 1f : 0f);
        szAnim.update(h ? 1f : 2f);
        if (!h && alpAnim.getValue() <= 0.01f) {
            lstHT = false;
            lstSqEnt = null;
            return;
        }
        Vector3d bp;
        float hh;
        if (h) {
            bp = MathUtil.interpolate(t, e.getPartialTicks());
            hh = t.getHeight() / 2f;
            lstSqPos = bp;
            lstSqHH = hh;
            lstHT = true;
            lstSqEnt = t;
        } else {
            Entity f = lstSqEnt;
            if (f != null && f.isAlive()) {
                bp = MathUtil.interpolate(f, e.getPartialTicks());
                hh = f.getHeight() / 2f;
                lstSqPos = bp;
                lstSqHH = hh;
            } else {
                if (lstSqPos == null || !lstHT) return;
                bp = lstSqPos;
                hh = lstSqHH;
            }
        }
        MatrixStack m = new MatrixStack();
        m.translate(bp.x - cp.x, bp.y + hh - cp.y, bp.z - cp.z);
        m.rotate(cm.getRotation().copy());
        float hf = h && ((LivingEntity) t).maxHurtTime > 0 ? MathHelper.clamp((float) ((LivingEntity) t).hurtTime / ((LivingEntity) t).maxHurtTime, 0f, 1f) : 0f;
        htScAnim.update(MathHelper.lerp(hf, 1f, 0.5f));
        htClAnim.update(hf);
        float sz = szAnim.getValue() * htScAnim.getValue();
        long n = System.currentTimeMillis();
        double dt = sqRotMs == 0 ? 0 : (n - sqRotMs) / 1000.0;
        sqRotMs = n;
        sqRotPh += 1.667 * (h ? 1 : 1.5) * dt;
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(m.getLast().getMatrix());
        RenderSystem.rotatef((float) (Math.sin(sqRotPh) * 180), 0, 0, 1);
        RenderSystem.disableDepthTest();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        int bc = ThemeEditor.getColor(ThemeSettings.MODULE_VISUAL);
        float mx = htClAnim.getValue();
        int cl = ColorUtil.getColor(MathHelper.clamp((int) (ColorUtil.red(bc) * (1f - mx) + 255 * mx), 0, 255), MathHelper.clamp((int) (ColorUtil.green(bc) * (1f - mx)), 0, 255), MathHelper.clamp((int) (ColorUtil.blue(bc) * (1f - mx)), 0, 255), (int) (ThemeEditor.getAlpha(ThemeSettings.MODULE_VISUAL) * MathHelper.clamp(alpAnim.getValue(), 0f, 1f)));
        RenderUtil.drawImage3D(new ResourceLocation("hynix/icons/world_render/target.png"), -sz / 2f, -sz / 2f, 0, sz, sz, cl, true);
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableDepthTest();
        RenderSystem.popMatrix();
    }

    @EventTarget
    private void renderCircle(EventRender3D e) {
        AttackAura a = hynix.getInstance().getModuleManager().attackAura;
        if (!a.isEnabled() || !a.getTargetesp().is("Кольцо")) return;
        LivingEntity t = AttackAura.getTarget();
        if (t == null) return;
        float hf = t.maxHurtTime > 0 ? MathHelper.clamp((float) t.hurtTime / t.maxHurtTime, 0f, 1f) : 0f;
        float r = t.getWidth() * 0.8F;
        Vector3d v = MathUtil.interpolate(t, e.getPartialTicks());
        double d = 3000, el = System.currentTimeMillis() % d;
        boolean s = el > d / 2;
        double p = el / (d / 2);
        p = s ? p - 1 : 1 - p;
        p = p < 0.5 ? 2 * p * p : 1 - Math.pow(-2 * p + 2, 2) / 2;
        double ea = (t.getHeight() / 2) * (p > 0.5 ? 1 - p : p) * (s ? -1 : 1);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        MatrixStack st = new MatrixStack();
        Vector3d o = mc.getRenderManager().info.getProjectedView();
        st.push();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableCull();
        GlStateManager.depthMask(false);
        RenderSystem.lineWidth(2f);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        b.begin(8, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; ++i) {
            int bc = ColorUtil.gradientModule(i * 5);
            int cl = ColorUtil.getColor(MathHelper.clamp((int) (ColorUtil.red(bc) * (1f - hf) + 60 * hf), 0, 255), MathHelper.clamp((int) (ColorUtil.green(bc) * (1f - hf)), 0, 255), MathHelper.clamp((int) (ColorUtil.blue(bc) * (1f - hf)), 0, 255), 255);
            double c = Math.cos(Math.toRadians(i)), si = Math.sin(Math.toRadians(i));
            b.pos(st.getLast().getMatrix(), (float) (v.x + c * r - o.x), (float) (v.y + t.getHeight() * p - o.y), (float) (v.z + si * r - o.z)).color(cl).endVertex();
            b.pos(st.getLast().getMatrix(), (float) (v.x + c * r - o.x), (float) (v.y + t.getHeight() * p + ea - o.y), (float) (v.z + si * r - o.z)).color(ColorUtil.applyOpacity(cl, 0)).endVertex();
        }
        Tessellator.getInstance().draw();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        b.begin(2, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; ++i) {
            int bc = ColorUtil.gradientModule(i * 5);
            double c = Math.cos(Math.toRadians(i)), si = Math.sin(Math.toRadians(i));
            b.pos(st.getLast().getMatrix(), (float) (v.x + c * r - o.x), (float) (v.y + t.getHeight() * p - o.y), (float) (v.z + si * r - o.z)).color(ColorUtil.getColor(MathHelper.clamp((int) (ColorUtil.red(bc) * (1f - hf) + 60 * hf), 0, 255), MathHelper.clamp((int) (ColorUtil.green(bc) * (1f - hf)), 0, 255), MathHelper.clamp((int) (ColorUtil.blue(bc) * (1f - hf)), 0, 255), 255)).endVertex();
        }
        Tessellator.getInstance().draw();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();
        RenderSystem.shadeModel(7424);
        GlStateManager.depthMask(true);
        st.pop();
    }

    @EventTarget
    private void handleGhost(EventRender3D e) {
        AttackAura a = hynix.getInstance().getModuleManager().attackAura;
        boolean en = a.isEnabled() && a.getTargetesp().is("Призраки");
        LivingEntity t = en ? AttackAura.getTarget() : null;
        boolean al = t != null && t.isAlive();
        ghApAnim.update(en && al ? 1f : 0f);
        if (ghApAnim.getValue() <= 0.01f) {
            lstGhEnt = null;
            lstGhPos = null;
            ghPhMs = 0;
            for (int i = 0; i < 3; i++) ghPh[i] = 0;
            return;
        }
        Vector3d cp = mc.getRenderManager().info.getProjectedView();
        Entity te = al ? (lstGhEnt = t) : lstGhEnt;
        if (te == null || !te.isAlive()) {
            if (lstGhPos == null) return;
        } else {
            lstGhPos = MathUtil.interpolate(te, e.getPartialTicks()).add(0, te.getHeight() / 1.75, 0);
        }
        Vector3d bs = lstGhPos;
        float hf = al && t.maxHurtTime > 0 ? MathHelper.clamp((float) t.hurtTime / t.maxHurtTime, 0f, 1f) : 0f;
        htClAnim.update(hf);
        int bc = ThemeEditor.getColor(ThemeSettings.MODULE_VISUAL);
        float mx = htClAnim.getValue();
        int cl = ColorUtil.getColor(MathHelper.clamp((int) (ColorUtil.red(bc) * (1f - mx) + 255 * mx), 0, 255), MathHelper.clamp((int) (ColorUtil.green(bc) * (1f - mx)), 0, 255), MathHelper.clamp((int) (ColorUtil.blue(bc) * (1f - mx)), 0, 255), (int) ThemeEditor.getAlpha(ThemeSettings.MODULE_VISUAL));
        long n = System.currentTimeMillis();
        long dt = ghPhMs == 0 ? 0 : n - ghPhMs;
        ghPhMs = n;
        final double[] bS = {-0.25, -0.3, 0.25}, hS = {-0.3, -0.35, 0.3}, xS = {1, -1, -1}, yC = {0.4, 0.3, -0.4}, yB = {0.7, 0.08, -0.7}, zS = {-1, -1, 1};
        final boolean[] yX = {false, true, true};
        float av = ghApAnim.getValue();
        for (int v = 0; v < 3; v++) {
            double sp = MathHelper.lerp(mx, (float) bS[v], (float) hS[v]);
            ghPh[v] += (v == 1 ? -sp : sp) * dt / 42.5;
            for (int i = 0; i < 25; i++) {
                float pr = 1f - i / 25f;
                float sz = 0.1f + 0.25f * pr;
                double ag = ghPh[v] - i * 10 * (v == 1 ? -sp : sp) / 42.5;
                double ox = Math.sin(ag) * 0.7, oy = Math.cos(ag) * 0.7;
                MatrixStack m = new MatrixStack();
                m.translate(bs.x - cp.getX() + xS[v] * ox, bs.y - cp.getY() + (yX[v] ? ox * yC[v] : oy * yC[v]) + yB[v], bs.z - cp.getZ() + zS[v] * oy);
                m.rotate(mc.getRenderManager().info.getRotation().copy());
                RenderSystem.pushMatrix();
                RenderSystem.disableDepthTest();
                RenderSystem.multMatrix(m.getLast().getMatrix());
                RenderUtil.drawImage3D(new ResourceLocation("hynix/icons/world_render/glow.png"), -sz / 2f, -sz / 2f, 0f, sz, sz, ColorUtil.applyOpacity(cl, (int) (ColorUtil.alpha(cl) * av)), false);
                RenderSystem.enableDepthTest();
                RenderSystem.popMatrix();
            }
        }
    }
}