package su.hynix.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import mods.baritone.api.api.java.baritone.api.BaritoneAPI;
import net.minecraft.entity.player.PlayerEntity;
import su.hynix.events.EventUpdate;
import su.hynix.handlers.impl.StaffHandler;
import su.hynix.hynix;
import su.hynix.managers.impl.staffmanager.Staff;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.misc.ServerUtil;

import java.util.List;

public class AutoLeave extends Module {
    private final MultiBooleanSetting leave = new MultiBooleanSetting("Ливать если", new BooleanSetting("Мало здоровья", true), new BooleanSetting("Рядом игрок", true), new BooleanSetting("Администратор в ванише", true));
    private final ModeSetting typeleave = new ModeSetting("Тип ухода", "Спавн", () -> leave.is("Мало здоровья") || leave.is("Рядом игрок") || leave.is("Администратор в ванише"), "Спавн", "Домой", "Хаб");
    private final BooleanSetting pvp = new BooleanSetting("Ливать только в конце пвп", false, () -> leave.is("Мало здоровья") || leave.is("Рядом игрок") || leave.is("Администратор в ванише"));
    private final SliderSetting hp = new SliderSetting("Здоровье", 15, 1, 20, 1, () -> leave.is("Мало здоровья"));
    private final SliderSetting radius = new SliderSetting("Радиус", 10, 1, 50, 1, () -> leave.is("Рядом игрок"));
    private final BooleanSetting baritone = new BooleanSetting("Отключать баритон", true, () -> leave.is("Мало здоровья") || leave.is("Рядом игрок") || leave.is("Администратор в ванише"));

    public AutoLeave() {
        super("AutoLeave", "Избегает нежелательной встречи с игроками, выбранным способом, обеспечивая безопасность ", Category.Player);
        addSettings(leave, typeleave, pvp, hp, radius, baritone);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;
        if (leave.is("Мало здоровья") && mc.player.getHealth() <= hp.get()) {
            handleLeave();
            return;
        }

        if (leave.is("Рядом игрок")) {
            List<? extends PlayerEntity> players = mc.world.getPlayers();
            for (PlayerEntity player : players) {
                if (player == mc.player) continue;
                if (hynix.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
                if (mc.player.getDistance(player) <= radius.get()) {
                    handleLeave();
                    return;
                }
            }
        }


        if (leave.is("Администратор в ванише")) {
            List<Staff> vanishedStaff = StaffHandler.getVanishedStaff();
            if (!vanishedStaff.isEmpty()) {
                handleLeave();
            }
        }
    }


    private void handleLeave() {
        if (pvp.get() && !ServerUtil.isPvP()) {
            return;
        }

        if (baritone.get()) {
            String prefix = BaritoneAPI.getSettings().prefix.value;

            if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
                mc.player.sendChatMessage(prefix + "stop");
            }
        }

        switch (typeleave.get()) {
            case "Спавн":
                mc.player.sendChatMessage("/spawn");
                break;

            case "Домой":
                mc.player.sendChatMessage("/home");
                break;

            case "Хаб":
                mc.player.sendChatMessage("/hub");
                break;
        }

        toggle();
    }
}
