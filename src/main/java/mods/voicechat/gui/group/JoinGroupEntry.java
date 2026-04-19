package mods.voicechat.gui.group;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.voicechat.Voicechat;
import mods.voicechat.gui.GameProfileUtils;
import mods.voicechat.gui.GroupType;
import mods.voicechat.gui.widgets.ListScreenBase;
import mods.voicechat.gui.widgets.ListScreenEntryBase;
import mods.voicechat.voice.common.ClientGroup;
import mods.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupEntry extends ListScreenEntryBase<JoinGroupEntry> {

    protected static final ResourceLocation LOCK = new ResourceLocation(Voicechat.MODID + "/textures/icons/lock.png");
    protected static final ITextComponent GROUP_MEMBERS = new TranslationTextComponent("message.voicechat.group_members").mergeStyle(TextFormatting.GRAY);
    protected static final ITextComponent NO_GROUP_MEMBERS = new TranslationTextComponent("message.voicechat.no_group_members").mergeStyle(TextFormatting.GRAY);

    protected static final int SKIN_SIZE = 12;
    protected static final int PADDING = 4;
    protected static final int BG_FILL = ColorHelper.PackedColor.packColor(255, 74, 74, 74);
    protected static final int BG_FILL_SELECTED = ColorHelper.PackedColor.packColor(255, 90, 90, 90);
    protected static final int PLAYER_NAME_COLOR = ColorHelper.PackedColor.packColor(255, 255, 255, 255);

    protected final ListScreenBase parent;
    protected final Minecraft minecraft;
    protected final Group group;

    public JoinGroupEntry(ListScreenBase parent, Group group) {
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();
        this.group = group;
    }

    @Override
    public void render(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        if (hovered) {
            AbstractGui.fill(poseStack, left, top, left + width, top + height, BG_FILL_SELECTED);
        } else {
            AbstractGui.fill(poseStack, left, top, left + width, top + height, BG_FILL);
        }

        boolean hasPassword = group.group.hasPassword();

        if (hasPassword) {
            poseStack.push();
            poseStack.translate(left + PADDING, top + height / 2F - 8F, 0F);
            poseStack.scale(16F / 12F, 16F / 12F, 1F);
            minecraft.getTextureManager().bindTexture(LOCK);
            Screen.blit(poseStack, 0, 0, 0, 0, 12, 12, 16, 16);
            poseStack.pop();
        }

        StringTextComponent groupName = new StringTextComponent(group.group.name());
        minecraft.fontRenderer.func_243248_b(poseStack, groupName, left + PADDING + (hasPassword ? 16 + PADDING : 0), top + height / 2 - minecraft.fontRenderer.FONT_HEIGHT / 2, PLAYER_NAME_COLOR);

        int textWidth = minecraft.fontRenderer.getStringPropertyWidth(groupName) + (hasPassword ? 16 + PADDING : 0);

        int headsPerRow = (width - (PADDING + textWidth + PADDING + PADDING)) / (SKIN_SIZE + 1);
        int rows = 2;

        for (int i = 0; i < group.members.size(); i++) {
            PlayerState state = group.members.get(i);

            int headXIndex = i / rows;
            int headYIndex = i % rows;

            if (i >= headsPerRow * rows) {
                break;
            }

            int headPosX = left + width - SKIN_SIZE - PADDING - headXIndex * (SKIN_SIZE + 1);
            int headPosY = top + height / 2 - ((SKIN_SIZE * 2 + 2) / 2) + ((SKIN_SIZE * 2 + 2) / 2) * headYIndex;

            poseStack.push();
            minecraft.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));
            poseStack.translate(headPosX, headPosY, 0);
            float scale = (float) SKIN_SIZE / 8F;
            poseStack.scale(scale, scale, scale);
            Screen.blit(poseStack, 0, 0, 8, 8, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            Screen.blit(poseStack, 0, 0, 40, 8, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            poseStack.pop();
        }

        if (!hovered) {
            return;
        }
        List<IReorderingProcessor> tooltip = Lists.newArrayList();

        if (group.getGroup().type().equals(mods.voicechat.api.Group.Type.NORMAL)) {
            tooltip.add(new TranslationTextComponent("message.voicechat.group_title", new StringTextComponent(group.getGroup().name())).func_241878_f());
        } else {
            tooltip.add(new TranslationTextComponent("message.voicechat.group_type_title", new StringTextComponent(group.getGroup().name()), GroupType.fromType(group.getGroup().type()).getTranslation()).func_241878_f());
        }
        if (group.getMembers().isEmpty()) {
            tooltip.add(NO_GROUP_MEMBERS.func_241878_f());
        } else {
            tooltip.add(GROUP_MEMBERS.func_241878_f());
            int maxMembers = 10;
            for (int i = 0; i < group.getMembers().size(); i++) {
                if (i >= maxMembers) {
                    tooltip.add(new TranslationTextComponent("message.voicechat.more_members", group.getMembers().size() - maxMembers).mergeStyle(TextFormatting.GRAY).func_241878_f());
                    break;
                }
                PlayerState state = group.getMembers().get(i);
                tooltip.add(new StringTextComponent("  " + state.getName()).mergeStyle(TextFormatting.GRAY).func_241878_f());
            }
        }

        parent.postRender(() -> {
            parent.renderTooltip(poseStack, tooltip, mouseX, mouseY);
        });
    }

    public Group getGroup() {
        return group;
    }

    public static class Group {
        private final ClientGroup group;
        private final List<PlayerState> members;

        public Group(ClientGroup group) {
            this.group = group;
            this.members = new ArrayList<>();
        }

        public ClientGroup getGroup() {
            return group;
        }

        public List<PlayerState> getMembers() {
            return members;
        }
    }

}
