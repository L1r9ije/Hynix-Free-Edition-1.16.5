package mods.voicechat.api.events;

import mods.voicechat.api.ClientVoicechatSocket;

import javax.annotation.Nullable;

public interface ClientVoicechatInitializationEvent extends ClientEvent {

    /**
     * @return the custom socket implementation or <code>null</code> to use voice chats default one
     */
    @Nullable
    ClientVoicechatSocket getSocketImplementation();

    /**
     * Sets a custom implementation of the socket used for client side voice chat traffic.
     *
     * @param socket the custom socket implementation
     */
    void setSocketImplementation(ClientVoicechatSocket socket);

}
