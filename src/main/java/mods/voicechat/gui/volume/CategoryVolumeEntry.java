package mods.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.voicechat.VoicechatClient;
import mods.voicechat.plugins.impl.VolumeCategoryImpl;
import mods.voicechat.voice.client.ClientManager;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class CategoryVolumeEntry extends VolumeEntry {

    protected final VolumeCategoryImpl category;
    protected final ResourceLocation texture;

    public CategoryVolumeEntry(VolumeCategoryImpl category, AdjustVolumesScreen screen) {
        super(screen, new CategoryVolumeConfigEntry(category.id()));
        this.category = category;
        this.texture = ClientManager.getCategoryManager().getTexture(category.id(), OTHER_VOLUME_ICON);
    }

    public VolumeCategoryImpl getCategory() {
        return category;
    }

    @Override
    public void renderElement(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        minecraft.getTextureManager().bindTexture(texture);
        AbstractGui.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
        minecraft.fontRenderer.func_243248_b(poseStack, new StringTextComponent(category.name()), (float) textX, (float) textY, PLAYER_NAME_COLOR);
        if (hovered && category.description() != null) {
            screen.postRender(() -> {
                screen.renderTooltip(poseStack, new StringTextComponent(category.description()), mouseX, mouseY);
            });
        }
    }

    private record CategoryVolumeConfigEntry(String category) implements AdjustVolumeSlider.VolumeConfigEntry {

        @Override
        public void save(double value) {
            VoicechatClient.VOLUME_CONFIG.setCategoryVolume(category, value);
            VoicechatClient.VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.VOLUME_CONFIG.getCategoryVolume(category);
        }
    }

}
