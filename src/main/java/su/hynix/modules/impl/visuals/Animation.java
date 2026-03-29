package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import su.hynix.events.EventRenderChunk;
import su.hynix.events.EventRenderChunkContainer;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.MultiBooleanSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.utils.animation.Easing;
import su.hynix.utils.animation.Easings;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Animation extends Module {

    private final WeakHashMap<ChunkRenderDispatcher.ChunkRender, AtomicLong> renderChunkMap = new WeakHashMap<>();
    public MultiBooleanSetting mode = new MultiBooleanSetting("Что анимировать", new BooleanSetting("Список игроков", false), new BooleanSetting("Инвентарь", false), new BooleanSetting("Приближение камеры", false), new BooleanSetting("Обновление чанков", false), new BooleanSetting("Изменение перспективы", false));
    private final SliderSetting chunkSpeed = new SliderSetting("Скорость", 6.0F, 2.0F, 10.0F, 1.0F, () -> mode.is("Обновление чанков"));
    public ModeSetting chunkanim = new ModeSetting("Анимация чанков", "Quart", () -> mode.is("Обновление чанков"), "Quart", "Circ", "Sine", "Cubic");

    public Animation() {
        super("Animation", "Анимирует выбранные действия", Category.Visuals);
        addSettings(mode, chunkanim, chunkSpeed);
    }

    private double applySelectedEasing(double t) {
        String modeName = chunkanim.get();
        Easing easing;
        if ("Circ".equalsIgnoreCase(modeName)) {
            easing = Easings.CIRC_OUT;
        } else if ("Sine".equalsIgnoreCase(modeName)) {
            easing = Easings.SINE_OUT;
        } else if ("Cubic".equalsIgnoreCase(modeName)) {
            easing = Easings.CUBIC_OUT;
        } else {
            easing = Easings.QUART_OUT;
        }
        return easing.ease(t);
    }

    @EventTarget
    private void onEvent(EventRenderChunk event) {
        if (!Boolean.TRUE.equals(mode.is("Обновление чанков"))) {
            return;
        }
        if (mc.player != null && mc.world != null) {
            if (!renderChunkMap.containsKey(event.getChunkRender())) {
                renderChunkMap.put(event.getChunkRender(), new AtomicLong(-1L));
            }
        }
    }

    @EventTarget
    private void onEvent(EventRenderChunkContainer event) {
        if (!Boolean.TRUE.equals(mode.is("Обновление чанков"))) {
            return;
        }
        if (renderChunkMap.containsKey(event.getChunkRender())) {
            AtomicLong timeAlive = renderChunkMap.get(event.getChunkRender());
            long timeClone = timeAlive.get();
            if (timeClone == -1L) {
                timeClone = System.currentTimeMillis();
                timeAlive.set(timeClone);
            }

            long timeDifference = System.currentTimeMillis() - timeClone;
            double durationMs = chunkSpeed.get() * 100;
            if (timeDifference <= durationMs) {
                double chunkY = event.getChunkRender().getPosition().getY();
                double t = timeDifference / durationMs;
                double offsetY = chunkY * applySelectedEasing(t);
                RenderSystem.translated(0.0D, -chunkY + offsetY, 0.0D);
            }
        }
    }

}
