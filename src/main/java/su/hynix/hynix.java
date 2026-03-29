package su.hynix;

import com.darkmagician6.eventapi.EventManager;
import lombok.Getter;
import mods.cape.Cape;
import mods.proxy.Config;
import mods.proxy.ProxyServer;
import mods.viaversion.viamcp.ViaMCP;
import mods.voicechat.ForgeVoicechatClientMod;
import mods.voicechat.ForgeVoicechatMod;
import net.minecraft.util.text.StringTextComponent;
import su.hynix.commands.CommandManager;
import su.hynix.component.ComponentManager;
import su.hynix.handlers.HandlerManager;
import su.hynix.managers.impl.*;
import su.hynix.managers.impl.dragmanager.DraggingManager;
import su.hynix.managers.impl.staffmanager.StaffManager;
import su.hynix.modules.ModuleManager;
import su.hynix.ui.gui.DropDown;

@Getter
public class hynix {

    @Getter
    private static hynix instance;
    public DropDown dropDown;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private ThemeManager themeManager;
    private AccountManager accountManager;
    private ClientConfig clientConfig;
    private FriendManager friendManager;
    private StaffManager staffManager;
    private MacroManager macroManager;
    private WaypointManager waypointManager;
    private BlockESPManager blockESPManager;
    private NukerManager nukerManager;
    private CommandManager commandsManager;
    private DraggingManager draggingManager;
    private HandlerManager handlerManager;
    private ComponentManager componentManager;
    private Cape cape;

    public hynix() {
        instance = this;
        init();
    }

    private void init() {
        initMods();
        initManager();

        EventManager.register(this);
    }

    private void initManager() {
        draggingManager = new DraggingManager();
        draggingManager.init();

        accountManager = new AccountManager();
        accountManager.init();

        themeManager = new ThemeManager();
        themeManager.init();

        configManager = new ConfigManager();
        configManager.init();

        moduleManager = new ModuleManager();
        moduleManager.init();

        friendManager = new FriendManager();
        friendManager.init();

        staffManager = new StaffManager();
        staffManager.init();

        macroManager = new MacroManager();
        macroManager.init();

        waypointManager = new WaypointManager();
        waypointManager.init();

        blockESPManager = new BlockESPManager();
        blockESPManager.init();

        nukerManager = new NukerManager();
        nukerManager.init();

        componentManager = new ComponentManager();
        componentManager.init();

        clientConfig = new ClientConfig();
        clientConfig.init();

        handlerManager = new HandlerManager();
        handlerManager.init();


        clientConfig.applySettings();

        dropDown = new DropDown(new StringTextComponent(""));

        clientConfig.initializeDefaultTheme();

        commandsManager = new CommandManager();

//        discordRPCManager = new DiscordRPCManager();
//        discordRPCManager.update();

    }

    private void initMods() {
        new ForgeVoicechatMod();
        new ForgeVoicechatClientMod();
        new ProxyServer();
        Config.loadConfig();
        cape = new Cape();
        cape.init();
        try {
            ViaMCP.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        draggingManager.save();
        if (clientConfig != null && ClientConfig.getCurrentActiveTheme() != null) {
            clientConfig.onThemeApplied(ClientConfig.getCurrentActiveTheme());
        }
        clientConfig.saveCurrentSettings();
    }
}
