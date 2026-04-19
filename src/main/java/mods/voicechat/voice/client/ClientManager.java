package mods.voicechat.voice.client;

import io.netty.channel.local.LocalAddress;
import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.VoicechatGate;
import mods.voicechat.debug.DebugOverlay;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;
import mods.voicechat.net.ClientServerNetManager;
import mods.voicechat.net.RequestSecretPacket;
import mods.voicechat.net.SecretPacket;
import mods.voicechat.voice.server.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientManager {

    private static ClientManager instance;
    private final ClientPlayerStateManager playerStateManager;
    private final ClientGroupManager groupManager;
    private final ClientCategoryManager categoryManager;
    private final PTTKeyHandler pttKeyHandler;
    private final RenderEvents renderEvents;
    private final DebugOverlay debugOverlay;
    private final KeyEvents keyEvents;
    private final Minecraft minecraft;
    @Nullable
    private ClientVoicechat client;

    private ClientManager() {
        playerStateManager = new ClientPlayerStateManager();
        groupManager = new ClientGroupManager();
        categoryManager = new ClientCategoryManager();
        pttKeyHandler = new PTTKeyHandler();
        renderEvents = new RenderEvents();
        debugOverlay = new DebugOverlay();
        keyEvents = new KeyEvents();
        minecraft = Minecraft.getInstance();

        ClientCompatibilityManager.INSTANCE.onJoinWorld(this::onJoinWorld);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
        ClientCompatibilityManager.INSTANCE.onPublishServer(this::onPublishServer);

        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(connection -> {
            if (client != null) {
                client.onVoiceChatConnected(connection);
            }
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(() -> {
            if (client != null) {
                client.onVoiceChatDisconnected();
            }
        });

        ClientServerNetManager.setClientListener(CommonCompatibilityManager.INSTANCE.getNetManager().secretChannel, (client, handler, packet) -> authenticate(packet));
    }

    private static String resolveAddress(SocketAddress socketAddress) throws IOException {
        if (socketAddress instanceof LocalAddress) {
            return "127.0.0.1";
        }
        if (!(socketAddress instanceof InetSocketAddress address)) {
            throw new IOException(String.format("Failed to determine server address with SocketAddress of type %s", socketAddress.getClass().getSimpleName()));
        }
        InetAddress inetAddress = address.getAddress();
        if (inetAddress == null) {
            return address.getHostString();
        }
        return inetAddress.getHostAddress();
    }

    @Nullable
    public static ClientVoicechat getClient() {
        return instance().client;
    }

    public static ClientPlayerStateManager getPlayerStateManager() {
        return instance().playerStateManager;
    }

    public static ClientGroupManager getGroupManager() {
        return instance().groupManager;
    }

    public static ClientCategoryManager getCategoryManager() {
        return instance().categoryManager;
    }

    public static PTTKeyHandler getPttKeyHandler() {
        return instance().pttKeyHandler;
    }

    public static RenderEvents getRenderEvents() {
        return instance().renderEvents;
    }

    public static DebugOverlay getDebugOverlay() {
        return instance().debugOverlay;
    }

    public static synchronized ClientManager instance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    private void authenticate(SecretPacket secretPacket) {
        if (!VoicechatGate.isEnabled()) {
            return;
        }
        if (client == null) {
            Voicechat.LOGGER.error("Received secret without a client being present");
            return;
        }
        Voicechat.LOGGER.info("Received secret");
        if (client.getConnection() != null) {
            ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
        }
        ClientPlayNetHandler connection = minecraft.getConnection();
        if (connection != null) {
            try {
                SocketAddress socketAddress = ClientCompatibilityManager.INSTANCE.getSocketAddress(connection.getNetworkManager());
                client.connect(new InitializationData(resolveAddress(socketAddress), secretPacket));
            } catch (Exception e) {
                Voicechat.LOGGER.error("Failed to connect to voice chat server", e);
            }
        }
    }

    private void onJoinWorld() {
        if (!VoicechatGate.isEnabled()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.muteOnJoin.get()) {
            playerStateManager.setMuted(true);
        }
        if (client != null) {
            Voicechat.LOGGER.info("Disconnecting from previous connection due to server change");
            onDisconnect();
        }
        client = new ClientVoicechat();
        Voicechat.LOGGER.info("Sending secret request to the server");
        ClientServerNetManager.sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
    }

    private void onDisconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
        ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
    }

    private void onPublishServer(int port) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        try {
            Voicechat.LOGGER.info("Changing voice chat port to {}", port);
            server.changePort(port);
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                ClientVoicechatConnection connection = client.getConnection();
                if (connection != null) {
                    Voicechat.LOGGER.info("Force disconnecting due to port change");
                    connection.disconnect();
                }
            }
            ClientServerNetManager.sendToServer(new RequestSecretPacket(Voicechat.COMPATIBILITY_VERSION));
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to change voice chat port", e);
        }
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new TranslationTextComponent("message.voicechat.server_port", server.getPort()));
    }

    public KeyEvents getKeyEvents() {
        return keyEvents;
    }

}
