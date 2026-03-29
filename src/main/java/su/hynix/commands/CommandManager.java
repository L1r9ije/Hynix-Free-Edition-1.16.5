package su.hynix.commands;

import com.mojang.brigadier.CommandDispatcher;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import su.hynix.commands.impl.*;
import su.hynix.utils.Wrapper;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class CommandManager implements Wrapper {
    private final CommandDispatcher<ISuggestionProvider> dispatcher = new CommandDispatcher<>();
    private final List<Command> commands = new ArrayList<>();
    private String prefix = ".";

    public CommandManager() {
        initialize();
    }

    public ISuggestionProvider getSource() {
        ClientPlayNetHandler conn = mc.getConnection();
        if (conn != null) {
            return conn.getSuggestionProvider();
        }
        return new ClientSuggestionProvider(null, mc);
    }

    private void initialize() {
        add(new ConfigCommand());
        add(new FriendCommand());
        add(new GpsCommand());
        add(new BindCommand());
        add(new StaffCommand());
        add(new MacroCommand());
        add(new PrefixCommand());
        add(new HelpCommand());
        add(new WaypointCommand());
        add(new BlockESPCommand());
        add(new NukerCommand());
        add(new RctCommand());
        add(new AutoContractCommand());
    }

    private void add(Command command) {
        command.add(dispatcher);
        commands.add(command);
    }
}