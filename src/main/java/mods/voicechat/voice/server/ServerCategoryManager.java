package mods.voicechat.voice.server;

import mods.voicechat.Voicechat;
import mods.voicechat.net.AddCategoryPacket;
import mods.voicechat.net.NetManager;
import mods.voicechat.net.RemoveCategoryPacket;
import mods.voicechat.plugins.CategoryManager;
import mods.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class ServerCategoryManager extends CategoryManager {

    private final Server server;

    public ServerCategoryManager(Server server) {
        this.server = server;
    }

    public void onPlayerCompatibilityCheckSucceeded(ServerPlayerEntity player) {
        Voicechat.LOGGER.debug("Synchronizing {} volume categories with {}", categories.size(), player.getName().getString());
        for (VolumeCategoryImpl category : getCategories()) {
            broadcastAddCategory(server.getServer(), category);
        }
    }

    @Override
    public void addCategory(VolumeCategoryImpl category) {
        super.addCategory(category);
        Voicechat.LOGGER.debug("Synchronizing volume category {} with all players", category.getId());
        broadcastAddCategory(server.getServer(), category);
    }

    @Override
    @Nullable
    public VolumeCategoryImpl removeCategory(String categoryId) {
        VolumeCategoryImpl volumeCategory = super.removeCategory(categoryId);
        Voicechat.LOGGER.debug("Removing volume category {} for all players", categoryId);
        broadcastRemoveCategory(server.getServer(), categoryId);
        return volumeCategory;
    }

    private void broadcastAddCategory(MinecraftServer server, VolumeCategoryImpl category) {
        AddCategoryPacket packet = new AddCategoryPacket(category);
        server.getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void broadcastRemoveCategory(MinecraftServer server, String categoryId) {
        RemoveCategoryPacket packet = new RemoveCategoryPacket(categoryId);
        server.getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

}
