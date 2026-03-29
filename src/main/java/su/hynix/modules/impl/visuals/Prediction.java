package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import su.hynix.events.EventRender2D;
import su.hynix.events.EventRender3D;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.Wrapper;
import su.hynix.utils.render.ColorUtil;
import su.hynix.utils.render.ProjectUtil;
import su.hynix.utils.render.RenderUtil;
import su.hynix.utils.render.font.Fonts;

import java.text.DecimalFormat;

public class Prediction extends Module implements Wrapper {

    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.0");
    private final MultiBooleanSetting projectiles = new MultiBooleanSetting("Снаряды", new BooleanSetting("Эндер Пёрл", true), new BooleanSetting("Стрела", true), new BooleanSetting("Трезубец", true));

    public Prediction() {
        super("Prediction", "Предугадывает куда и за сколько времени упадет предмет", Category.Visuals);
        addSettings(projectiles);
    }

    @EventTarget
    public void onEvent(EventRender2D event) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (validEntity(entity) && noMove(entity)) {
                Item item = entity instanceof EnderPearlEntity ? Items.ENDER_PEARL : entity instanceof ArrowEntity ? Items.ARROW : Items.TRIDENT;
                Vector3d pearlPosition = entity.getPositionVec();
                Vector3d pearlMotion = entity.getMotion();
                Vector3d lastPosition = pearlPosition;

                int steps = 0;
                for (int i = 0; i <= 300; i++) {
                    steps++;
                    lastPosition = pearlPosition;
                    pearlPosition = pearlPosition.add(pearlMotion);
                    pearlMotion = updatePearlMotion(entity, pearlMotion, pearlPosition);

                    if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                        break;
                    }
                }

                Vector2f position = ProjectUtil.project2D(lastPosition.x, lastPosition.y, lastPosition.z);
                if (position.x == Float.MAX_VALUE && position.y == Float.MAX_VALUE) continue;

                float timeInSeconds = steps * 0.05f;


                float x = position.x;
                float y = position.y + 3;

                String timeText = TIME_FORMAT.format(timeInSeconds) + " сек.";
                float timeWidth = Fonts.sf_medium[14].getWidth(timeText);

                RenderUtil.drawMinecraftRectangle(event.getStack(), x - 20, y - 3, timeWidth + 15.5f, 11, ColorUtil.getColor(0, 0, 0, 60));
                RenderUtil.drawMinecraftRectangle(event.getStack(), x - 20, y - 3, timeWidth + 15.5f, 11, ColorUtil.getColor(0, 0, 0, 120));
                ResourceLocation icon = item == Items.ENDER_PEARL ? new ResourceLocation("textures/item/ender_pearl.png") : item == Items.ARROW ? new ResourceLocation("textures/item/arrow.png") : new ResourceLocation("textures/item/trident.png");
                RenderUtil.drawImage2D(icon, x - 18, y - 1.5f, 8, 8, -1);
                Fonts.sf_medium[14].drawString(event.getStack(), timeText, x - (timeWidth / 2) + 6, y + 0.5f, -1);
            }
        }
    }

    @EventTarget
    public void onEvent(EventRender3D event) {
        MatrixStack matrix = new MatrixStack();
        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrix.getLast().getMatrix());
        RenderSystem.translated(-mc.getRenderManager().renderPosX(), -mc.getRenderManager().renderPosY(), -mc.getRenderManager().renderPosZ());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1.5F);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        BUFFER.begin(1, DefaultVertexFormats.POSITION_COLOR);
        for (Entity entity : mc.world.getAllEntities()) {
            if (validEntity(entity) && noMove(entity)) renderLine(entity);
        }
        TESSELLATOR.draw();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.translated(mc.getRenderManager().renderPosX(), mc.getRenderManager().renderPosY(), mc.getRenderManager().renderPosZ());
        RenderSystem.popMatrix();
    }

    private void renderLine(Entity pearl) {
        Vector3d pearlPosition = pearl.getPositionVec().add(0, 0, 0);
        Vector3d pearlMotion = pearl.getMotion();
        Vector3d lastPosition;
        for (int i = 0; i <= 300; i++) {
            lastPosition = pearlPosition;
            pearlPosition = pearlPosition.add(pearlMotion);
            pearlMotion = updatePearlMotion(pearl, pearlMotion, lastPosition);

            if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                break;
            }

            int color = ThemeEditor.getColor(ThemeSettings.MODULE_VISUAL);
            BUFFER.pos(lastPosition.x, lastPosition.y, lastPosition.z).color(color).endVertex();
            BUFFER.pos(pearlPosition.x, pearlPosition.y, pearlPosition.z).color(color).endVertex();
        }
    }

    public Vector3d updatePearlMotion(Entity entity, Vector3d originalPearlMotion, Vector3d pearlPosition) {
        Vector3d pearlMotion = originalPearlMotion;

        if ((entity.isInWater() || mc.world.getBlockState(new BlockPos(pearlPosition)).getBlock() == Blocks.WATER) && !(entity instanceof TridentEntity)) {
            float scale = entity instanceof EnderPearlEntity ? 0.8f : 0.6f;
            pearlMotion = pearlMotion.scale(scale);
        } else {
            pearlMotion = pearlMotion.scale(0.99f);
        }

        if (!entity.hasNoGravity()) pearlMotion.y -= entity instanceof EnderPearlEntity ? 0.03 : 0.05;

        return pearlMotion;
    }

    public boolean shouldEntityHit(Vector3d pearlPosition, Vector3d lastPosition) {
        final RayTraceContext rayTraceContext = new RayTraceContext(lastPosition, pearlPosition, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player);
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

        return blockHitResult.getType() == RayTraceResult.Type.BLOCK;
    }

    boolean noMove(Entity entity) {
        return entity.prevPosY != entity.getPosY() || entity.prevPosX != entity.getPosX() || entity.prevPosZ != entity.getPosZ();
    }

    boolean validEntity(Entity entity) {
        return (entity instanceof EnderPearlEntity && projectiles.is("Эндер Пёрл")) || (entity instanceof ArrowEntity && projectiles.is("Стрела")) || (entity instanceof TridentEntity && projectiles.is("Трезубец"));
    }
}
