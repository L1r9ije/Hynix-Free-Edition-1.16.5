package mods.voicechat;

import com.sun.jna.Platform;
import de.maxhenkel.configbuilder.ConfigBuilder;
import mods.voicechat.config.ClientConfig;
import mods.voicechat.config.VolumeConfig;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;
import mods.voicechat.macos.VersionCheck;
import mods.voicechat.plugins.impl.opus.OpusManager;
import mods.voicechat.profile.UsernameCache;
import mods.voicechat.resourcepacks.VoiceChatResourcePack;
import mods.voicechat.voice.client.ClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public abstract class VoicechatClient {

    public static ClientConfig CLIENT_CONFIG;
    public static VolumeConfig VOLUME_CONFIG;
    public static UsernameCache USERNAME_CACHE;

    public static VoiceChatResourcePack CLASSIC_ICONS;
    public static VoiceChatResourcePack WHITE_ICONS;
    public static VoiceChatResourcePack BLACK_ICONS;

    public VoicechatClient() {
        CLASSIC_ICONS = new VoiceChatResourcePack("classic_icons", new TranslationTextComponent("resourcepack.voicechat.classic_icons"));
        WHITE_ICONS = new VoiceChatResourcePack("white_icons", new TranslationTextComponent("resourcepack.voicechat.white_icons"));
        BLACK_ICONS = new VoiceChatResourcePack("black_icons", new TranslationTextComponent("resourcepack.voicechat.black_icons"));

        ClientCompatibilityManager.INSTANCE.addResourcePackSource(Minecraft.getInstance().getResourcePackList(), (Consumer<ResourcePackInfo> consumer, ResourcePackInfo.IFactory packConstructor) -> {
            consumer.accept(CLASSIC_ICONS.toPack());
            consumer.accept(WHITE_ICONS.toPack());
            consumer.accept(BLACK_ICONS.toPack());
        });
    }

    public void initializeConfigs() {
        fixVolumeConfig();
        CLIENT_CONFIG = ConfigBuilder.builder(ClientConfig::new).path(Voicechat.getVoicechatConfigFolder().resolve("voicechat-client.properties")).build();
        VOLUME_CONFIG = new VolumeConfig(Voicechat.getVoicechatConfigFolder().resolve("voicechat-volumes.properties"));
        USERNAME_CACHE = new UsernameCache(Voicechat.getVoicechatConfigFolder().resolve("username-cache.json").toFile());
    }

    public void initializeClient() {
        initializeConfigs();

        //Load instance
        ClientManager.instance();

        OpusManager.opusNativeCheck();

        if (Platform.isMac()) {
            if (!VersionCheck.isMacOSNativeCompatible()) {
                Voicechat.LOGGER.warn("Your MacOS version is incompatible with {}", CommonCompatibilityManager.INSTANCE.getModName());
            }
            if (!CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
                CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
            }
        }
    }

    private void fixVolumeConfig() {
        Path oldLocation = Voicechat.getConfigFolder().resolve("voicechat-volumes.properties");
        Path newLocation = Voicechat.getVoicechatConfigFolder().resolve("voicechat-volumes.properties");
        if (!newLocation.toFile().exists() && oldLocation.toFile().exists()) {
            try {
                Files.move(oldLocation, newLocation, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to move volumes config", e);
            }
        }
    }
}
