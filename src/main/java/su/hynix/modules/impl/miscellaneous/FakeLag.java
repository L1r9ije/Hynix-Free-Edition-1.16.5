package su.hynix.modules.impl.miscellaneous;


import com.darkmagician6.eventapi.EventTarget;
import com.google.common.base.Stopwatch;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.math.vector.Vector3d;
import su.hynix.events.*;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.StopWatch;
import su.hynix.utils.math.TimeUtil;
import su.hynix.utils.misc.ChatUtil;
import su.hynix.utils.render.ColorUtil;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FakeLag extends Module {

    public final StopWatch часы = new StopWatch();
    final StopWatch стопер = new StopWatch();
    private final BooleanSetting пульс = new BooleanSetting("Пульс", false);
    private final BooleanSetting рендер = new BooleanSetting("Рендер позиции", false);
    private final SliderSetting лаг = new SliderSetting("Пинг", 100F, 100F, 2000F, 25F, пульс::get);
    private final BooleanSetting атака = new BooleanSetting("Обновлять при атаке", false);
    private final CopyOnWriteArrayList<IPacket> пакеты = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService планировщик =
            Executors.newSingleThreadScheduledExecutor();
    TimeUtil asd = new TimeUtil();
    boolean стоп = false;
    boolean цвет = false;
    Vector3d прошлаяПозиция = new Vector3d(0, 0, 0);
    private long начался;
    // Новые поля
    private IPacket отложенныйПакет = null;
    private boolean ждётАтаку = false;
    public FakeLag() {
        super("Fake Lag", "Создаёт фейковый пинг на сервере", Category.Combat);
        addSettings(пульс, лаг, атака);
    }

    @EventTarget
    public void приОбновленииИгры(EventGameUpdate e) {
        if (mc.player == null || mc.world == null && this.isEnabled())
            toggle();
    }

    @EventTarget
    public void приПакете(EventPacket e) {
        if (mc.player != null && mc.world != null && !mc.isSingleplayer() && !mc.player.getShouldBeDead()) {

            // Если ждём отправки атаки — этот пакет блокируем
            if (ждётАтаку && e.getPacket() instanceof CUseEntityPacket) {
                e.setCancelled(true);
                return;
            }

            boolean возврат = !цвет && e.getPacket() instanceof CUseEntityPacket && e.isSend();
            if (e.isSend()) {
                if (!возврат || !атака.get()) пакеты.add(e.getPacket());
                e.setCancelled(true);
            }

        } else toggle();
    }


    @EventTarget
    public void приОбновлении(EventMotion e) {
        if ((System.currentTimeMillis() - начался) >= 29900) {
            toggle();
        }
        if ((часы.finished(лаг.get()) && пульс.get())
                || (стоп && атака.get() && стопер.finished(20))) {
            прошлаяПозиция = mc.player.getPositionVec();
            for (IPacket пакет : пакеты) {
                mc.player.connection.getNetworkManager().sendPacketWithoutEvent(пакет);
            }

            стоп = false;
            пакеты.clear();
            начался = System.currentTimeMillis();
            часы.reset();
        }
    }


    // --- ВАЖНО: полностью изменённый обработчик атаки ---
    @EventTarget
    public void приАтаке(EventAttack e) {
        if (!атака.get()) return;

        double dist = e.getTarget().getDistance(
                mc.player
        );

        // Если ближе — обычное поведение FakeLag
        if (dist < 4.5) {
            стопер.reset();
            стоп = true;
            цвет = true;
            return;
        }

        // ДАЛЬШАЯ АТАКА → перехватываем
        e.setCancelled(true);
        цвет = false;

        ждётАтаку = true;
        отложенныйПакет = new CUseEntityPacket(e.getTarget(), mc.player.isSneaking());

        boolean былоВключено = this.isEnabled();
        if (былоВключено) this.toggle(); // временно выключаем FakeLag

        // Отправляем через 1 миллисекунду
        планировщик.schedule(() -> {

            if (отложенныйПакет != null) {
                mc.player.connection.getNetworkManager().sendPacketWithoutEvent(отложенныйПакет);
            }

            отложенныйПакет = null;
            ждётАтаку = false;

            if (!this.isEnabled() && былоВключено)
                this.toggle(); // включаем FakeLag обратно

        }, 1, TimeUnit.MILLISECONDS);
    }


    @EventTarget
    public void приРендере(EventRender3D e) {
//        if (рендер.get()) {
//            float halfSize = 0.3f;
//            VoxelShape форма = VoxelShapes.create(
//                    прошлаяПозиция.x - halfSize,
//                    прошлаяПозиция.y - halfSize + 0.3,
//                    прошлаяПозиция.z - halfSize,
//                    прошлаяПозиция.x + halfSize,
//                    прошлаяПозиция.y + halfSize + 1.5,
//                    прошлаяПозиция.z + halfSize
//            );
//
//            int цвет = атака.get()
//                    ? this.цвет ? ColorUtil.getColor(0, 255, 0) : ColorUtil.getColor(255, 0, 0)
//                    : ColorUtil.getColor(255, 255, 255);
//
//            RenderUtil3D.drawChams(e.getMatrix(), форма, прошлаяПозиция, цвет, 0.1f, true);
//        }
    }


    @Override
    public void onEnable() {
        super.onEnable();
        начался = System.currentTimeMillis();
        прошлаяПозиция = mc.player.getPositionVec();
        часы.reset();
        стоп = false;
        стопер.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        for (IPacket пакет : пакеты) {
            mc.player.connection.getNetworkManager().sendPacketWithoutEvent(пакет);
        }

        часы.reset();
        пакеты.clear();
        стоп = false;
        стопер.reset();
    }
}

