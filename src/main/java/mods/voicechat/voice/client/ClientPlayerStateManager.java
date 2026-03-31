package mods.voicechat.voice.client;

import mods.voicechat.Voicechat;
import mods.voicechat.VoicechatClient;
import mods.voicechat.api.events.ClientVoicechatConnectionEvent;
import mods.voicechat.api.events.MicrophoneMuteEvent;
import mods.voicechat.api.events.VoicechatDisableEvent;
import mods.voicechat.gui.CreateGroupScreen;
import mods.voicechat.gui.EnterPasswordScreen;
import mods.voicechat.gui.group.GroupList;
import mods.voicechat.gui.group.GroupScreen;
import mods.voicechat.gui.group.JoinGroupList;
import mods.voicechat.gui.group.JoinGroupScreen;
import mods.voicechat.gui.onboarding.OnboardingManager;
import mods.voicechat.gui.volume.AdjustVolumeList;
import mods.voicechat.intercompatibility.ClientCompatibilityManager;
import mods.voicechat.intercompatibility.CommonCompatibilityManager;
import mods.voicechat.net.ClientServerNetManager;
import mods.voicechat.net.UpdateStatePacket;
import mods.voicechat.plugins.PluginManager;
import mods.voicechat.plugins.impl.events.ClientVoicechatConnectionEventImpl;
import mods.voicechat.plugins.impl.events.MicrophoneMuteEventImpl;
import mods.voicechat.plugins.impl.events.VoicechatDisableEventImpl;
import mods.voicechat.voice.common.ClientGroup;
import mods.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ClientPlayerStateManager {

    private boolean disconnected;
    @Nullable
    private UUID group;

    private Map<UUID, PlayerState> states;

    public ClientPlayerStateManager() {
        this.disconnected = true;
        this.group = null;

        states = new HashMap<>();

        ClientServerNetManager.setClientListener(CommonCompatibilityManager.INSTANCE.getNetManager().playerStateChannel, (client, handler, packet) -> {
            states.put(packet.getPlayerState().getUuid(), packet.getPlayerState());
            Voicechat.LOGGER.debug("Got state for {}: {}", packet.getPlayerState().getName(), packet.getPlayerState());
            VoicechatClient.USERNAME_CACHE.updateUsernameAndSave(packet.getPlayerState().getUuid(), packet.getPlayerState().getName());
            if (packet.getPlayerState().isDisconnected()) {
                ClientVoicechat c = ClientManager.getClient();
                if (c != null) {
                    c.closeAudioChannel(packet.getPlayerState().getUuid());
                }
            }
            AdjustVolumeList.update();
            JoinGroupList.update();
            GroupList.update();
        });
        ClientServerNetManager.setClientListener(CommonCompatibilityManager.INSTANCE.getNetManager().playerStatesChannel, (client, handler, packet) -> {
            states = packet.getPlayerStates();
            Voicechat.LOGGER.debug("Received {} state(s)", states.size());
            for (PlayerState state : states.values()) {
                VoicechatClient.USERNAME_CACHE.updateUsername(state.getUuid(), state.getName());
            }
            VoicechatClient.USERNAME_CACHE.save();
            AdjustVolumeList.update();
            JoinGroupList.update();
            GroupList.update();
        });
        ClientServerNetManager.setClientListener(CommonCompatibilityManager.INSTANCE.getNetManager().joinedGroupChannel, (client, handler, packet) -> {
            Screen screen = Minecraft.getInstance().currentScreen;
            this.group = packet.getGroup();
            if (packet.isWrongPassword()) {
                if (screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                    Minecraft.getInstance().displayGuiScreen(null);
                }
                client.player.sendStatusMessage(new TranslationTextComponent("message.voicechat.wrong_password").mergeStyle(TextFormatting.DARK_RED), true);
            } else if (group != null && screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                ClientGroup clientGroup = getGroup();
                if (clientGroup != null) {
                    Minecraft.getInstance().displayGuiScreen(new GroupScreen(clientGroup));
                } else {
                    Voicechat.LOGGER.warn("Received join group packet without group being present");
                }
            }
            GroupList.update();
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(this::onVoiceChatConnected);
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(this::onVoiceChatDisconnected);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
    }

    private void resetOwnState() {
        disconnected = true;
        group = null;
    }

    /**
     * Called when the voicechat client gets disconnected or the player logs out
     */
    public void onVoiceChatDisconnected() {
        disconnected = true;
        syncOwnState();
        PluginManager.instance().dispatchEvent(ClientVoicechatConnectionEvent.class, new ClientVoicechatConnectionEventImpl(false));
    }

    /**
     * Called when the voicechat client gets (re)connected
     */
    public void onVoiceChatConnected(ClientVoicechatConnection client) {
        disconnected = false;
        syncOwnState();
        PluginManager.instance().dispatchEvent(ClientVoicechatConnectionEvent.class, new ClientVoicechatConnectionEventImpl(true));
    }

    private void onDisconnect() {
        clearStates();
        resetOwnState();
    }

    public boolean isPlayerDisabled(PlayerEntity player) {
        PlayerState playerState = states.get(player.getUniqueID());
        if (playerState == null) {
            return false;
        }

        return playerState.isDisabled();
    }

    public boolean isPlayerDisconnected(PlayerEntity player) {
        PlayerState playerState = states.get(player.getUniqueID());
        if (playerState == null) {
            return VoicechatClient.CLIENT_CONFIG.showFakePlayersDisconnected.get();
        }

        return playerState.isDisconnected();
    }

    public void syncOwnState() {
        ClientServerNetManager.sendToServer(new UpdateStatePacket(isDisabled()));
        Voicechat.LOGGER.debug("Sent own state to server: disabled={}", isDisabled());
    }

    public boolean isDisabled() {
        if (!canEnable()) {
            return true;
        }
        return VoicechatClient.CLIENT_CONFIG.disabled.get();
    }

    public void setDisabled(boolean disabled) {
        VoicechatClient.CLIENT_CONFIG.disabled.set(disabled).save();
        syncOwnState();
        PluginManager.instance().dispatchEvent(VoicechatDisableEvent.class, new VoicechatDisableEventImpl(disabled));
    }

    public boolean canEnable() {
        if (OnboardingManager.isOnboarding()) {
            return false;
        }
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return false;
        }
        return client.getSoundManager() != null;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public boolean isMuted() {
        return VoicechatClient.CLIENT_CONFIG.muted.get();
    }

    public void setMuted(boolean muted) {
        VoicechatClient.CLIENT_CONFIG.muted.set(muted).save();
        PluginManager.instance().dispatchEvent(MicrophoneMuteEvent.class, new MicrophoneMuteEventImpl(muted));
    }

    public void onFinishOnboarding() {
        syncOwnState();
    }

    public boolean isInGroup(PlayerEntity player) {
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            return false;
        }
        return state.hasGroup();
    }

    @Nullable
    public UUID getGroup(PlayerEntity player) {
        PlayerState state = states.get(player.getUniqueID());
        if (state == null) {
            return null;
        }
        return state.getGroup();
    }

    @Nullable
    public ClientGroup getGroup() {
        if (group == null) {
            return null;
        }
        return ClientManager.getGroupManager().getGroup(group);
    }

    @Nullable
    public UUID getGroupID() {
        return group;
    }

    public List<PlayerState> getPlayerStates(boolean includeSelf) {
        if (includeSelf) {
            return new ArrayList<>(states.values());
        } else {
            return states.values().stream().filter(playerState -> !playerState.getUuid().equals(getOwnID())).collect(Collectors.toList());
        }
    }

    public UUID getOwnID() {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            ClientVoicechatConnection connection = client.getConnection();
            if (connection != null) {
                return connection.getData().getPlayerUUID();
            }
        }
        return Minecraft.getInstance().getSession().getProfile().getId();
    }

    @Nullable
    public PlayerState getState(UUID player) {
        return states.get(player);
    }

    public void clearStates() {
        states.clear();
    }
}
