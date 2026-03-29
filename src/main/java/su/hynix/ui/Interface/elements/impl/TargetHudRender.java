package su.hynix.ui.Interface.elements.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import su.hynix.events.EventRender2D;
import su.hynix.hynix;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.impl.combat.AttackAura;
import su.hynix.modules.impl.miscellaneous.ScoreboardHealth;
import su.hynix.ui.Interface.elements.ElementRender;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.math.MathUtil;
import su.hynix.utils.misc.ServerUtil;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class TargetHudRender implements ElementRender {

    public static final ModeSetting hpbar = new ModeSetting("Цвет здоровья", "Клиентский", "Клиентский", "Здоровье");
    public static final BooleanSetting goldhealth = new BooleanSetting("Золотые сердца", true);
    public static final BooleanSetting particles2 = new BooleanSetting("Частицы", true);
    public static final BooleanSetting ontarget = new BooleanSetting("При наведении", false);
    public static final BooleanSetting armor = new BooleanSetting("Отображать броню", false);
    public static final BooleanSetting alphabg = new BooleanSetting("Прозрачный фон", false);

    private final Dragging dragging;
    private final DecimalFormat healthFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    private final AnimationUtil healthAnimation = new AnimationUtil(0.0f, 3, Easings.CUBIC_OUT);
    private final AnimationUtil secondaryHealthAnimation = new AnimationUtil(0.0f, 3, Easings.LINEAR);
    private final AnimationUtil alphaAnimation = new AnimationUtil(0.0f, 10f, Easings.CUBIC_OUT);
    private final AnimationUtil armorScaleAnimation = new AnimationUtil(1.0f, 15f, Easings.CUBIC_OUT);
    private final AnimationUtil absorptionAnimation = new AnimationUtil(0.0f, 3, Easings.CUBIC_OUT);
    private final AnimationUtil secondaryAbsorptionAnimation = new AnimationUtil(0.0f, 3, Easings.LINEAR);
    @Getter
    private final CopyOnWriteArrayList<HeadParticle> particles = new CopyOnWriteArrayList<>();
    private Entity lastTarget = null;
    private float lastHurtTime = 0;

    @Override
    public void render(EventRender2D.Post event) {
        float posX = dragging.getX(), posY = dragging.getY();
        MatrixStack ms = event.getStack();

        LivingEntity auraTarget = AttackAura.getTarget();
        Entity target = determineTarget(auraTarget);
        float targetAlpha = target != null ? 1.0f : 0.0f;

        Entity entityToUpdate = target != null ? target : lastTarget;
        if (entityToUpdate instanceof LivingEntity livingEntityToUpdate) {
            float currentHP = getHealth(livingEntityToUpdate);
            float absorption = livingEntityToUpdate.getAbsorptionAmount();

            if (target != lastTarget && target != null) {
                healthAnimation.setValue(currentHP);
                secondaryHealthAnimation.setValue(currentHP);
                absorptionAnimation.setValue(absorption);
                secondaryAbsorptionAnimation.setValue(absorption);
            }

            healthAnimation.update(currentHP);
            secondaryHealthAnimation.update(currentHP);
            absorptionAnimation.update(absorption);
            secondaryAbsorptionAnimation.update(absorption);
        }

        updateAnimations(targetAlpha);

        if (alphaAnimation.getValue() <= 0 && !particles.isEmpty()) {
            particles.clear();
        }

        if (alphaAnimation.getValue() > 0) {
            renderTargetHud(ms, target != null ? target : lastTarget, posX, posY);
        }

        lastTarget = target != null ? target : lastTarget;
        dragging.setHeight(38);
        dragging.setWidth(100);
    }

    private Entity determineTarget(LivingEntity auraTarget) {
        if (ontarget.get()) {
            if (mc.currentScreen instanceof ChatScreen) {
                return mc.player;
            }
            RayTraceResult ray = mc.objectMouseOver;
            if (ray != null && ray.getType() == RayTraceResult.Type.ENTITY) {
                Entity ent = ((EntityRayTraceResult) ray).getEntity();
                return ent instanceof LivingEntity ? ent : auraTarget;
            }
            return auraTarget;
        }
        return auraTarget == null && mc.currentScreen instanceof ChatScreen ? mc.player : auraTarget;
    }

    private float getHealth(LivingEntity entity) {
        boolean useScoreboard = hynix.getInstance().getModuleManager().getModule(ScoreboardHealth.class).isEnabled();
        if (useScoreboard) {
            float scoreboardHealth = ServerUtil.getHealth(entity);
            float normalHealth = entity.getHealth() + entity.getAbsorptionAmount();
            if (scoreboardHealth == Math.floor(normalHealth)) {
                return entity.getHealth();
            } else {
                return Math.max(0, scoreboardHealth);
            }
        } else {
            return entity.getHealth();
        }
    }

    private void updateAnimations(float targetAlpha) {
        alphaAnimation.update(targetAlpha);
        armorScaleAnimation.update(armor.get() && targetAlpha > 0 ? 1 : 0.0f);
    }


    private void renderTargetHud(MatrixStack ms, Entity renderTarget, float posX, float posY) {
        if (!(renderTarget instanceof LivingEntity livingTarget)) return;

        float alpha = alphaAnimation.getValue();
        float currentHurtTime = livingTarget.hurtTime > 0 ? Math.min(0.5f, (float) livingTarget.hurtTime / livingTarget.maxHurtTime) : 0;

        if (particles2.get() && currentHurtTime > lastHurtTime) {
            for (int i = 0; i < 16; ++i) {
                particles.add(new HeadParticle(new Vector3d(17, 17, 0.0)));
            }
        }
        lastHurtTime = currentHurtTime;

        float currentHP = getHealth(livingTarget);
        float absorption = livingTarget.getAbsorptionAmount();
        float maxHP = livingTarget.getMaxHealth();

        float hpPercentage = Math.min(healthAnimation.getValue() / maxHP, 1.0f);
        float secondaryHpPercentage = Math.min(secondaryHealthAnimation.getValue() / maxHP, 1.0f);
        float hpBarWidth = 52 * hpPercentage;
        float secondaryHpBarWidth = 52 * secondaryHpPercentage;

        int textColor = ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_TEXT), (ThemeEditor.getAlpha(ThemeSettings.WINDOW_TEXT) / 255f) * alpha);

        RenderUtil.drawBlurredRoundedRectangle(posX, posY, 100, 30, 5.0f, alphabg.get() ? ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.WINDOW_BG), 0) : ThemeEditor.getColor(ThemeSettings.WINDOW_BG), alpha);

        renderParticles(posX, posY, alpha);
        renderEntityHead(ms, renderTarget, posX, posY, textColor, alpha);
        if (armor.get()) {
            renderArmor(livingTarget, posX, posY);
        }
        renderHealthBar(posX, posY, hpBarWidth, secondaryHpBarWidth, currentHP, absorption, maxHP, alpha);
        renderText(ms, renderTarget, posX, posY, currentHP, absorption, textColor);
    }

    private void renderParticles(float posX, float posY, float alpha) {
        particles.removeIf(particle -> System.currentTimeMillis() - particle.time > particle.lifetime);
        for (HeadParticle particle : particles) {
            particle.update(posX, posY);
            float size = 1.0f - (float) (System.currentTimeMillis() - particle.time) / particle.lifetime;
            float radius = 2.3f;
            RenderUtil.drawRoundedRectangle((float) particle.pos.x - 3, (float) particle.pos.y - 3, radius * 2, radius * 2, radius - 1, ColorUtil.applyOpacity(ThemeEditor.getColor(ThemeSettings.MODULE_VISUAL), (int) (255.0f * particle.alpha * size * alpha)));
        }
    }

    private void renderEntityHead(MatrixStack ms, Entity renderTarget, float posX, float posY, int textColor, float alpha) {
        if (renderTarget instanceof PlayerEntity)
            RenderUtil.drawRoundedHead(mc.getRenderManager().getRenderer(renderTarget).getEntityTexture(renderTarget), (LivingEntity) renderTarget, posX + 3, posY + 3, 24, 24, 4, alpha);
        else Fonts.icons[36].drawString(ms, "N", posX + 6.5f, posY + 9.0f, textColor);
    }

    private void renderArmor(LivingEntity livingTarget, float posX, float posY) {
        List<ItemStack> armorStacks = new ArrayList<>();
        for (ItemStack stack : livingTarget.getArmorInventoryList()) {
            if (!stack.isEmpty()) {
                armorStacks.add(stack);
            }
        }
        int armorCount = armorStacks.size();
        ItemStack offhand = livingTarget.getHeldItemOffhand();
        ItemStack mainhand = livingTarget.getHeldItemMainhand();
        int totalItems = armorCount + (offhand.isEmpty() ? 0 : 1) + (mainhand.isEmpty() ? 0 : 1);

        if (totalItems > 0) {
            float panelWidth = dragging.getWidth();
            float usedWidth = 8 * totalItems + (totalItems - 1);
            float handX = posX + panelWidth - usedWidth - 3.5f;

            if (!offhand.isEmpty()) {
                RenderUtil.scaleStart(handX + 4, posY - 6, armorScaleAnimation.getValue());
                RenderUtil.drawStack(offhand, handX, posY - 10, 0.5f);
                RenderUtil.scaleEnd();
                handX += 9;
            }
            if (!mainhand.isEmpty()) {
                RenderUtil.scaleStart(handX + 4, posY - 6, armorScaleAnimation.getValue());
                RenderUtil.drawStack(mainhand, handX, posY - 10, 0.5f);
                RenderUtil.scaleEnd();
                handX += 9;
            }
            for (int i = 0; i < armorCount; i++) {
                float itemX = handX + i * 9;
                RenderUtil.scaleStart(itemX + 4, posY - 6, armorScaleAnimation.getValue());
                RenderUtil.drawStack(armorStacks.get(i), itemX, posY - 10, 0.5f);
                RenderUtil.scaleEnd();
            }
        }
    }

    private void renderHealthBar(float posX, float posY, float hpBarWidth, float secondaryHpBarWidth, float currentHP, float absorption, float maxHP, float alpha) {
        if (hpBarWidth <= 0) return;

        float barX = posX + 29.5F;
        float barY = posY + 20.0f;
        float barWidth = 52;
        float barHeight = 5.0f;
        float radius = 1.0f;

        if (hpbar.is("Клиентский")) {
            int activeColor = ThemeEditor.getColor(ThemeSettings.MODULE_VISUAL);
            int inactiveColor = ColorUtil.darken(activeColor, 0.5f);
            float baseAlpha = (ThemeEditor.getAlpha(ThemeSettings.MODULE_VISUAL) / 255f) * alpha;

            RenderUtil.drawRoundedRectangleGradient(barX, barY, barWidth, barHeight, radius, inactiveColor, inactiveColor, activeColor, activeColor, baseAlpha * 0.3f);
            RenderUtil.drawRoundedRectangleGradient(barX, barY, secondaryHpBarWidth, barHeight, radius, inactiveColor, inactiveColor, activeColor, activeColor, baseAlpha * 0.75f);
            RenderUtil.drawRoundedRectangleGradient(barX, barY, hpBarWidth, barHeight, radius, inactiveColor, inactiveColor, activeColor, activeColor, baseAlpha);
        } else {
            int[] colors = getHealthBarColors(currentHP, maxHP);
            RenderUtil.drawRoundedRectangleGradient(barX, barY, barWidth, barHeight, radius, colors[0], colors[0], colors[1], colors[1], 80f / 255f * alpha);
            RenderUtil.drawRoundedRectangleGradient(barX, barY, secondaryHpBarWidth, barHeight, radius, colors[2], colors[2], colors[3], colors[3], 140f / 255f * alpha);
            RenderUtil.drawRoundedRectangleGradient(barX, barY, hpBarWidth, barHeight, radius, colors[2], colors[2], colors[4], colors[4], alpha);
        }
        if (goldhealth.get() && absorption > 0f) {
            float animatedAbsorption = absorptionAnimation.getValue();
            float absorptionBarWidth = 58 * Math.min(animatedAbsorption / maxHP, 1.0f);

            float lagAbsorption = secondaryAbsorptionAnimation.getValue();
            float secondaryAbsorptionBarWidth = 58 * Math.min(lagAbsorption / maxHP, 1.0f);

            int goldTop = ColorUtil.getColor(255, 210, 0);
            int goldBottom = ColorUtil.darken(goldTop, 0.5f);

            RenderUtil.drawRoundedRectangleGradient(barX, barY, secondaryAbsorptionBarWidth, barHeight, radius, ColorUtil.darken(goldBottom, 0.6f), ColorUtil.darken(goldBottom, 0.6f), ColorUtil.darken(goldTop, 0.8f), ColorUtil.darken(goldTop, 0.8f), alpha * 0.6f);
            RenderUtil.drawRoundedRectangleGradient(barX, barY, absorptionBarWidth, barHeight, radius, goldBottom, goldBottom, goldTop, goldTop, alpha);
        }
    }

    private int[] getHealthBarColors(float currentHP, float maxHP) {
        if (currentHP >= maxHP * 0.7) {
            return new int[]{ColorUtil.getColor(0, 40, 8), ColorUtil.getColor(0, 80, 15), ColorUtil.getColor(0, 60, 12), ColorUtil.getColor(0, 160, 40), ColorUtil.getColor(0, 190, 45)};
        } else if (currentHP >= maxHP * 0.35) {
            return new int[]{ColorUtil.getColor(50, 55, 25), ColorUtil.getColor(85, 70, 50), ColorUtil.getColor(55, 50, 22), ColorUtil.getColor(140, 130, 60), ColorUtil.getColor(160, 150, 70)};
        } else {
            return new int[]{ColorUtil.getColor(50, 35, 25), ColorUtil.getColor(70, 45, 40), ColorUtil.getColor(80, 42, 32), ColorUtil.getColor(160, 90, 70), ColorUtil.getColor(180, 100, 75)};
        }
    }

    private void renderText(MatrixStack ms, Entity renderTarget, float posX, float posY, float currentHP, float absorption, int textColor) {
        String plainName = TextFormatting.getTextWithoutFormattingCodes(renderTarget.getName().getString());
        Fonts.sf_medium[16].drawSubString(ms, plainName, posX + 30.0F, posY + 6.5f, textColor, 47);
        Fonts.sf_medium[12].drawString(ms, currentHP > 799 ? "???" : healthFormat.format(currentHP), posX + 97.0f - Fonts.sf_medium[12].getWidth(currentHP > 799 ? "???" : healthFormat.format(currentHP)), posY + 21.5f, textColor);
        if (goldhealth.get() && absorption > 0) {
            String hpText = "HP: " + healthFormat.format(currentHP);
            String goldText = healthFormat.format(absorption);
            float goldX = posX + 30.0f;
            Fonts.sf_medium[12].drawString(ms, goldText, goldX, posY + 15.0005f, textColor);
        }
    }

    public static class HeadParticle {
        private final Vector3d endOffset;
        private final long time;
        private final long lifetime;
        private Vector3d offset;
        private Vector3d pos;
        private float alpha;

        public HeadParticle(Vector3d offset) {
            this.offset = offset;
            this.pos = offset;
            this.endOffset = offset.add(-ThreadLocalRandom.current().nextFloat(-75.0f, 75.0f), -ThreadLocalRandom.current().nextFloat(-75.0f, 75.0f), -ThreadLocalRandom.current().nextFloat(-75.0f, 75.0f));
            this.time = System.currentTimeMillis();
            this.lifetime = 1250 + ThreadLocalRandom.current().nextLong(750);
        }

        public void update(float hudX, float hudY) {
            this.alpha = MathUtil.lerp(this.alpha, 1.0f, 10.0f);
            this.offset = MathUtil.fast(this.offset, this.endOffset, 0.75f);
            this.pos = new Vector3d(hudX + this.offset.x, hudY + this.offset.y, this.offset.z);
        }
    }
}