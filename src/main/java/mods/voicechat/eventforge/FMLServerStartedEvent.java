package mods.voicechat.eventforge;

import net.minecraft.server.MinecraftServer;

public class FMLServerStartedEvent extends ServerLifecycleEvent {
    public FMLServerStartedEvent(MinecraftServer server) {
        super(server);
    }
}
