package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import su.hynix.events.EventKey;
import su.hynix.events.EventPacket;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.utils.math.TimeUtil;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.server.SChatPacket;


import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AutoDuel extends Module {
    private static final Pattern pattern = Pattern.compile("^\\w{3,16}$");
    private final ModeSetting mode = new ModeSetting("Режим", "Ball", "Ball", "Shield", "Spikes", "Netherite", "CheatParadise", "Bow", "Classic", "Totems", "NoDebuff");
    private final BindSetting disable = new BindSetting("Клавиша отключения");
    private final List<String> sent = Lists.newArrayList();
    private final TimeUtil counter = new TimeUtil();
    private int currentPlayerIndex = 0;

    public AutoDuel() {
        super("Auto Duel", "Автоматически отправляет вызов на дуэль игрокам", Category.Miscellaneous);
        addSettings(mode, disable);
    }


    @EventTarget
    public void onEvent(EventKey event) {
        if (disable.get() == event.getKey() && event.isHold()) {
            toggle();
        }
    }


    @EventTarget
    public void update(EventUpdate e) {
        List<String> players = mc.player.connection.getPlayerInfoMap().stream().map(NetworkPlayerInfo::getGameProfile).map(GameProfile::getName).filter((profileName) -> pattern.matcher(profileName).matches()).collect(Collectors.toList());
        if (counter.hasTimeElapsed(800L * players.size())) {
            sent.clear();
            currentPlayerIndex = 0;
            counter.reset();
        }

        if (!players.isEmpty()) {
            if (counter.hasTimeElapsed(1000)) {
                if (currentPlayerIndex >= players.size()) {
                    currentPlayerIndex = 0;
                }

                String player = players.get(currentPlayerIndex);
                if (!sent.contains(player) && !player.equals(mc.session.getProfile().getName())) {
                    mc.player.sendChatMessage("/duel " + player);
                    sent.add(player);
                }

                ++currentPlayerIndex;
                counter.reset();
            }

            if (mc.player.openContainer instanceof ChestContainer chest) {
                if (mc.currentScreen.getTitle().getString().contains("Выбор набора (1/1)")) {
                    if (counter.hasTimeElapsed(150)) {
                        mc.playerController.windowClick(chest.windowId, AutoDuelSlot.valueOf((mode.get())).ordinal(), 0, ClickType.QUICK_MOVE, mc.player);
                        counter.reset();
                    }
                } else if (mc.currentScreen.getTitle().getString().contains("Настройка поединка") && counter.hasTimeElapsed(150)) {
                    mc.playerController.windowClick(chest.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                    counter.reset();
                }
            }
        }
    }

    @EventTarget
    public void send(EventPacket event) {
        if (event.getPacket() instanceof SChatPacket chat) {
            String text = chat.getChatComponent().getString().toLowerCase();
            if (text.contains("начало") && text.contains("через") && text.contains("секунд!") || text.isEmpty()) {
                toggle();
            }
        }
    }

    public enum AutoDuelSlot {
        Shield(), Spikes(), Bow(), Totems(), NoDebuff(), Ball(), Classic(), CheatParadise(), Netherite()
    }
}