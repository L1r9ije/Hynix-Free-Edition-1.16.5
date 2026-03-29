package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.ChatScreen;
import su.hynix.events.EventRender2D;
import su.hynix.hynix;
import su.hynix.managers.impl.dragmanager.Dragging;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ButtonSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.ui.Interface.elements.impl.*;
import su.hynix.utils.render.font.Fonts;

import java.util.Locale;

public class Interface extends Module {

    public static ButtonSetting themeeditor = new ButtonSetting("ThemeEdit", false, "Открыть темки", "Закрыть темки");
    public static ButtonSetting autobuy = new ButtonSetting("AutoBuy", false, "Открыть меню Auto Buy", "Закрыть меню Auto Buy");
    public static MultiBooleanSetting elements = new MultiBooleanSetting("Выбор",
            new BooleanSetting("Броня", true),
            new BooleanSetting("Счетчик Тотемов", true),
            new BooleanSetting("Информация о цели", true),
            new BooleanSetting("Логотип", true),
            new BooleanSetting("Администрация онлайн", true),
            new BooleanSetting("Уведомления", true),
            new BooleanSetting("Задержки", true),
            new BooleanSetting("Зелья", true),
            new BooleanSetting("Горячие клавиши", true),
            new BooleanSetting("Координаты", true),
            new BooleanSetting("Скорость", true)
    );

    TargetHudRender targetHudRender;
    KeybindsRender keybindsRender;
    WatermarkRender watermarkRender;
    //InventoryRender inventoryRender;
    ArmorRender armorRender;
    TotemCounterRender totemCounterRender;
    NotificationRender notificationRender;
    PotionsRender potionsRender;
    CooldownsRender cooldownsRender;
    StaffListRender staffListRender;

    public Interface() {
        super("Interface", "Визуальный интерфейс клиента", Category.Visuals);

        Dragging targetHudDrug = new Dragging("Информация о цели", 4, 100, elements);
        Dragging keybinds = new Dragging("Горячие клавиши", 297, 100, elements);
        //Dragging inventory = new Dragging("Инвентарь", 4, 34, elements);
        Dragging potions = new Dragging("Зелья", 357, 100, elements);
        Dragging staff = new Dragging("Администрация онлайн", 447, 100, elements);
        Dragging cooldowns = new Dragging("Задержки", 217, 85, elements);
        Dragging watermarkDrag = new Dragging("Логотип", 4, 4, elements, false);
        Dragging notification = new Dragging("Уведомления", mc.getMainWindow().getScaledWidth() / 2f, mc.getMainWindow().getScaledHeight() / 2f + 21f, elements, false);

        targetHudRender = new TargetHudRender(targetHudDrug);
        keybindsRender = new KeybindsRender(keybinds);
        armorRender = new ArmorRender();
        totemCounterRender = new TotemCounterRender();
        watermarkRender = new WatermarkRender(watermarkDrag);
        notificationRender = new NotificationRender(notification);
        potionsRender = new PotionsRender(potions);
        cooldownsRender = new CooldownsRender(cooldowns);
        staffListRender = new StaffListRender(staff);

        //inventory.addSettings(InventoryRender.alphabg);
        keybinds.addSettings(KeybindsRender.alphabg);
        cooldowns.addSettings(CooldownsRender.alphabg);
        staff.addSettings(StaffListRender.skins, StaffListRender.alphabg);
        potions.addSettings(PotionsRender.badeffects, PotionsRender.alphabg);
        notification.addSettings(NotificationRender.shield, NotificationRender.spec, NotificationRender.warps, NotificationRender.module, NotificationRender.lowstrength, NotificationRender.effects, NotificationRender.alphabg);
        targetHudDrug.addSettings(TargetHudRender.hpbar, TargetHudRender.goldhealth, TargetHudRender.particles2, TargetHudRender.ontarget, TargetHudRender.armor, TargetHudRender.alphabg);
        watermarkDrag.addSettings(WatermarkRender.login, WatermarkRender.fps, WatermarkRender.time, WatermarkRender.coordinates, WatermarkRender.ping, WatermarkRender.tps, WatermarkRender.bps, WatermarkRender.logotype, WatermarkRender.alphabg);
        watermarkDrag.addSettings(WatermarkRender.login, WatermarkRender.fps, WatermarkRender.alphabg);

        addSettings(elements, themeeditor);
    }

    @EventTarget
    private void onRender(EventRender2D.Post e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        for (Dragging draggable : hynix.getInstance().getDraggingManager().getRenderOrder()) {
            String name = draggable.getName();
            if (elements.is(name) != null && elements.is(name)) {
                switch (name) {
                    case "Информация о цели":
                        targetHudRender.render(e);
                        break;
                    case "Горячие клавиши":
                        keybindsRender.render(e);
                        break;
                    //case "Инвентарь":
                    //    inventoryRender.render(e);
                    //    break;
                    case "Зелья":
                        potionsRender.render(e);
                        break;
                    case "Задержки":
                        cooldownsRender.render(e);
                        break;
                    case "Администрация онлайн":
                        staffListRender.render(e);
                        break;
                }
            }
        }

        if (elements.is("Счетчик Тотемов") != null && elements.is("Счетчик Тотемов")) {
            totemCounterRender.render(e);
        }
        if (elements.is("Броня") != null && elements.is("Броня")) {
            armorRender.render(e);
        }
        if (elements.is("Логотип") != null && elements.is("Логотип")) {
            watermarkRender.render(e);
        }
        if (elements.is("Координаты") != null && elements.is("Координаты")) {

            String text = (int) mc.player.getPosX() + ", " + (int) mc.player.getPosY() + ", " + (int) mc.player.getPosZ();

            float scalePosY = mc.getMainWindow().getScaledHeight() - Fonts.sf_medium[16].getHeight() - (float) (mc.currentScreen instanceof ChatScreen ? 7 * mc.gameSettings.guiScale : 0);

            Fonts.sf_medium[16].drawString(e.getStack(), "XYZ: " + text, 2.5f, scalePosY - 2.5f, -1);
        }
        if (elements.is("Скорость") != null && elements.is("Скорость")) {
            double dx = mc.player.getPosX() - mc.player.prevPosX;
            double dy = mc.player.getPosY() - mc.player.prevPosY;
            double dz = mc.player.getPosZ() - mc.player.prevPosZ;

            String text = String.format(Locale.ENGLISH, "%.1f", Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0).replace(',', '.');

            float scalePosY = mc.getMainWindow().getScaledHeight() - Fonts.sf_medium[16].getHeight() - (float) (mc.currentScreen instanceof ChatScreen ? 7 * mc.gameSettings.guiScale : 0);

            float offset = elements.is("Координаты") != null && elements.is("Координаты") ? 12.5f : 2.5f;

            Fonts.sf_medium[16].drawString(e.getStack(), "BPS: " + text, 2.5f, scalePosY - offset, -1);
        }
        if (elements.is("Уведомления") != null && elements.is("Уведомления")) {
            notificationRender.render(e);
        }
    }
}