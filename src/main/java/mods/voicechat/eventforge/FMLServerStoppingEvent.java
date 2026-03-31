package mods.voicechat.eventforge;

import net.minecraft.server.MinecraftServer;

public class FMLServerStoppingEvent extends ServerLifecycleEvent {
    public FMLServerStoppingEvent(MinecraftServer server) {
        super(server);
    }
}
