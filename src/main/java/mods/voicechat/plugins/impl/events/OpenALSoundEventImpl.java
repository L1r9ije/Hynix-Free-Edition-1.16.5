package mods.voicechat.plugins.impl.events;

import mods.voicechat.api.Position;
import mods.voicechat.api.events.OpenALSoundEvent;
import mods.voicechat.plugins.impl.PositionImpl;

import javax.annotation.Nullable;
import java.util.UUID;

public class OpenALSoundEventImpl extends ClientEventImpl implements OpenALSoundEvent, OpenALSoundEvent.Pre, OpenALSoundEvent.Post {

    @Nullable
    protected PositionImpl position;
    @Nullable
    protected UUID channelId;
    @Nullable
    protected String category;
    protected int source;

    public OpenALSoundEventImpl(@Nullable UUID channelId, @Nullable PositionImpl position, @Nullable String category, int source) {
        this.channelId = channelId;
        this.position = position;
        this.category = category;
        this.source = source;
    }

    @Override
    @Nullable
    public Position getPosition() {
        return position;
    }

    @Override
    @Nullable
    public UUID getChannelId() {
        return channelId;
    }

    @Override
    public int getSource() {
        return source;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
