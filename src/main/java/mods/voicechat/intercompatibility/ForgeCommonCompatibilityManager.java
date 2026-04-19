package mods.voicechat.intercompatibility;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import com.mojang.brigadier.CommandDispatcher;
import mods.voicechat.api.VoicechatPlugin;
import mods.voicechat.eventforge.FMLServerStartedEvent;
import mods.voicechat.eventforge.FMLServerStoppingEvent;
import mods.voicechat.eventforge.PlayerEvent;
import mods.voicechat.eventforge.RegisterCommandsEvent;
import mods.voicechat.events.ServerVoiceChatConnectedEvent;
import mods.voicechat.events.ServerVoiceChatDisconnectedEvent;
import mods.voicechat.events.VoiceChatCompatibilityCheckSucceededEvent;
import mods.voicechat.net.ForgeNetManager;
import mods.voicechat.net.NetManager;
import mods.voicechat.permission.ForgePermissionManager;
import mods.voicechat.permission.PermissionManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static su.hynix.utils.Wrapper.mc;

public class ForgeCommonCompatibilityManager extends CommonCompatibilityManager {

    private final List<Consumer<MinecraftServer>> serverStartingEvents;
    private final List<Consumer<MinecraftServer>> serverStoppingEvents;
    private final List<Consumer<CommandDispatcher<CommandSource>>> registerServerCommandsEvents;
    private final List<Consumer<ServerPlayerEntity>> playerLoggedInEvents;
    private final List<Consumer<ServerPlayerEntity>> playerLoggedOutEvents;
    private final List<Consumer<ServerPlayerEntity>> voicechatConnectEvents;
    private final List<Consumer<ServerPlayerEntity>> voicechatCompatibilityCheckSucceededEvents;
    private final List<Consumer<UUID>> voicechatDisconnectEvents;
    private ForgeNetManager netManager;

    public ForgeCommonCompatibilityManager() {
        serverStartingEvents = new CopyOnWriteArrayList<>();
        serverStoppingEvents = new CopyOnWriteArrayList<>();
        registerServerCommandsEvents = new CopyOnWriteArrayList<>();
        playerLoggedInEvents = new CopyOnWriteArrayList<>();
        playerLoggedOutEvents = new CopyOnWriteArrayList<>();
        voicechatConnectEvents = new CopyOnWriteArrayList<>();
        voicechatCompatibilityCheckSucceededEvents = new CopyOnWriteArrayList<>();
        voicechatDisconnectEvents = new CopyOnWriteArrayList<>();
    }

    @EventTarget
    public void serverStarting(FMLServerStartedEvent event) {
        serverStartingEvents.forEach(consumer -> consumer.accept(event.getServer()));
    }

    @EventTarget
    public void serverStopping(FMLServerStoppingEvent event) {
        serverStoppingEvents.forEach(consumer -> consumer.accept(event.getServer()));
    }

    @EventTarget
    public void onRegisterCommands(RegisterCommandsEvent event) {
        registerServerCommandsEvents.forEach(consumer -> consumer.accept(event.getDispatcher()));
    }

    @EventTarget
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity player) {
            playerLoggedInEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @EventTarget
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity player) {
            playerLoggedOutEvents.forEach(consumer -> consumer.accept(player));
        }
    }

    @Override
    public String getModVersion() {
        return "1.16.5";
    }

    @Override
    public String getModName() {
        return "Simple Voice Chat";
    }

    @Override
    public Path getGameDirectory() {
        return mc.gameDir.toPath();
    }

    @Override
    public void emitServerVoiceChatConnectedEvent(ServerPlayerEntity player) {
        voicechatConnectEvents.forEach(consumer -> consumer.accept(player));
        EventManager.call(new ServerVoiceChatConnectedEvent(player));
    }

    @Override
    public void emitServerVoiceChatDisconnectedEvent(UUID clientID) {
        voicechatDisconnectEvents.forEach(consumer -> consumer.accept(clientID));
        EventManager.call(new ServerVoiceChatDisconnectedEvent(clientID));
    }

    @Override
    public void emitPlayerCompatibilityCheckSucceeded(ServerPlayerEntity player) {
        voicechatCompatibilityCheckSucceededEvents.forEach(consumer -> consumer.accept(player));
        EventManager.call(new VoiceChatCompatibilityCheckSucceededEvent(player));
    }

    @Override
    public void onServerVoiceChatConnected(Consumer<ServerPlayerEntity> onVoiceChatConnected) {
        voicechatConnectEvents.add(onVoiceChatConnected);
    }

    @Override
    public void onServerVoiceChatDisconnected(Consumer<UUID> onVoiceChatDisconnected) {
        voicechatDisconnectEvents.add(onVoiceChatDisconnected);
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> onServerStarting) {
        serverStartingEvents.add(onServerStarting);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> onServerStopping) {
        serverStoppingEvents.add(onServerStopping);
    }

    @Override
    public void onPlayerLoggedIn(Consumer<ServerPlayerEntity> onPlayerLoggedIn) {
        playerLoggedInEvents.add(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<ServerPlayerEntity> onPlayerLoggedOut) {
        playerLoggedOutEvents.add(onPlayerLoggedOut);
    }

    @Override
    public void onPlayerCompatibilityCheckSucceeded(Consumer<ServerPlayerEntity> onPlayerCompatibilityCheckSucceeded) {
        voicechatCompatibilityCheckSucceededEvents.add(onPlayerCompatibilityCheckSucceeded);
    }

    @Override
    public void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSource>> onRegisterServerCommands) {
        registerServerCommandsEvents.add(onRegisterServerCommands);
    }

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new ForgeNetManager();
        }
        return netManager;
    }

    @Override
    public boolean isDevEnvironment() {
        return false;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return true;
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        List<VoicechatPlugin> plugins = new ArrayList<>();
//        ModList.get().getAllScanData().forEach(scan -> {
//            scan.getAnnotations().forEach(annotationData -> {
//                if (annotationData.getAnnotationType().getClassName().equals(ForgeVoicechatPlugin.class.getName())) {
//                    try {
//                        Class<?> clazz = Class.forName(annotationData.getMemberName());
//                        if (VoicechatPlugin.class.isAssignableFrom(clazz)) {
//                            VoicechatPlugin plugin = (VoicechatPlugin) clazz.getDeclaredConstructor().newInstance();
//                            plugins.add(plugin);
//                        }
//                    } catch (Throwable e) {
//                        Voicechat.LOGGER.warn("Failed to load plugin '{}'", annotationData.getMemberName(), e);
//                    }
//                }
//            });
//        });
        return plugins;
    }

    @Override
    public PermissionManager createPermissionManager() {
        return new ForgePermissionManager();
    }
}
