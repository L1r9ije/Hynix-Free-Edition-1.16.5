package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.server.SUpdateTimePacket;
import su.hynix.events.EventPacket;
import su.hynix.events.EventUpdate;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.ColorSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;

import java.time.LocalTime;

public class Ambience extends Module {
    public static ModeSetting timeMode = new ModeSetting("Время", "Не менять", "Рассвет", "Утро", "День", "Вечер", "Заход солнца", "Ночь", "Время из реальной жизни", "Не менять");
    public static ModeSetting fogMode = new ModeSetting("Туман", "Ничего не делать", "Ничего не делать", "Очистить", "Переопределить");
    public static ColorSetting fogColor = new ColorSetting("Цвет тумана", false, -1, () -> fogMode.is("Переопределить"));
    public static SliderSetting fogEnd = new SliderSetting("Конец тумана", 1.0f, 0.1f, 1.5f, 0.1f, () -> fogMode.is("Переопределить"));
    public static SliderSetting fogStart = new SliderSetting("Начало тумана", 0.5f, 0.1f, 1.5f, 0.1f, () -> fogMode.is("Переопределить"), fogEnd);

    public Ambience() {
        super("Ambience", "Позволяет изменить атмосферу игры", Category.Visuals);
        addSettings(timeMode, fogMode, fogColor, fogStart, fogEnd);
    }

    @EventTarget
    public void onUpdate(EventPacket event) {
        if (event.getPacket() instanceof SUpdateTimePacket && !timeMode.is("Не менять")) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!timeMode.get().equals("Не менять")) {
            long time;
            if (timeMode.get().equals("Время из реальной жизни")) {
                time = getRealWorldTime();
            } else {
                time = switch (timeMode.get()) {
                    case "Рассвет" -> 23000L;
                    case "Утро" -> 1000L;
                    case "День" -> 6000L;
                    case "Вечер" -> 12000L;
                    case "Заход солнца" -> 13000L;
                    case "Ночь" -> 18000L;
                    default -> mc.world.getDayTime();
                };
            }
            mc.world.setDayTime(time);
        }
    }

    private long getRealWorldTime() {
        LocalTime now = LocalTime.now();
        int hours = now.getHour();
        int minutes = now.getMinute();
        int seconds = now.getSecond();

        int totalSeconds = hours * 3600 + minutes * 60 + seconds;
        int offsetSeconds = (totalSeconds - 6 * 3600) % (24 * 3600);
        if (offsetSeconds < 0) offsetSeconds += 24 * 3600;

        return (long) ((offsetSeconds / 86400.0) * 24000);
    }
}