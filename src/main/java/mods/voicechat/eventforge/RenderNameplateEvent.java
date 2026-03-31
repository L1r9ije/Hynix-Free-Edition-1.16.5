package mods.voicechat.eventforge;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

public class RenderNameplateEvent extends EntityEvent {
    private final ITextComponent originalContent;
    private final EntityRenderer<?> entityRenderer;
    private final MatrixStack matrixStack;
    private final IRenderTypeBuffer renderTypeBuffer;
    private final int packedLight;
    private final float partialTicks;
    private ITextComponent nameplateContent;

    public RenderNameplateEvent(Entity entity, ITextComponent content, EntityRenderer<?> entityRenderer, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int packedLight, float partialTicks) {
        super(entity);
        this.originalContent = content;
        this.setContent(this.originalContent);
        this.entityRenderer = entityRenderer;
        this.matrixStack = matrixStack;
        this.renderTypeBuffer = renderTypeBuffer;
        this.packedLight = packedLight;
        this.partialTicks = partialTicks;
    }

    public ITextComponent getContent() {
        return this.nameplateContent;
    }

    public void setContent(ITextComponent contents) {
        this.nameplateContent = contents;
    }

    public ITextComponent getOriginalContent() {
        return this.originalContent;
    }

    public EntityRenderer<?> getEntityRenderer() {
        return this.entityRenderer;
    }

    public MatrixStack getStack() {
        return this.matrixStack;
    }

    public IRenderTypeBuffer getRenderTypeBuffer() {
        return this.renderTypeBuffer;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}
