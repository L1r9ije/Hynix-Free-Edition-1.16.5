package mods.voicechat.eventforge;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class RegisterCommandsEvent extends EventCancellable implements Event {
    private final CommandDispatcher<CommandSource> dispatcher;
    private final Commands.EnvironmentType environment;

    public RegisterCommandsEvent(CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType environment) {
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return this.dispatcher;
    }

    public Commands.EnvironmentType getEnvironment() {
        return this.environment;
    }
}
