package su.hynix.modules.impl.visuals;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.settings.PointOfView;
import org.joml.Vector2f;
import su.hynix.component.impl.RotationComponent;
import su.hynix.events.EventCancelThirdPerson;
import su.hynix.events.EventKey;
import su.hynix.events.EventThirdPersonDistance;
import su.hynix.events.EventUpdate;
import su.hynix.handlers.impl.LookHandler;
import su.hynix.handlers.impl.Rotation;
import su.hynix.hynix;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BindSetting;
import su.hynix.modules.api.constructors.impl.ModeSetting;
import su.hynix.modules.api.constructors.impl.SliderSetting;
import su.hynix.modules.impl.movement.FreeCamera;

public class ThirdPerson extends Module {
    public BindSetting keySetting = new BindSetting("Бинд");
    private final ModeSetting povMode = new ModeSetting("Режим обзора", "Сзади", "Спереди", "Сзади");
    private final SliderSetting distance = new SliderSetting("Дистанция", 4, 1, 10, 0.1F);
    private PointOfView prevPointOfView = PointOfView.FIRST_PERSON;
    private boolean pressed;
    private boolean keyDown;
    private final Vector2f rotation = new Vector2f(0.0F, 0.0F);


    public ThirdPerson() {
        super("Third Person", "Позволяет осмотреться от третьего лица без отображения другим игрокам", Category.Visuals);
        addSettings(povMode, keySetting, distance);
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (hynix.getInstance().getModuleManager().getModule(FreeCamera.class).isEnabled()) return;
        if (event.getKey() == keySetting.get()) {
            keyDown = event.isHold();
        }
    }

    @EventTarget
    public void onKey(EventThirdPersonDistance eventThirdPersonRender) {
        if (pressed) eventThirdPersonRender.setDistance(distance.get());
    }

    @EventTarget
    private void onUpdate(EventUpdate e) {
        if (!pressed) {
            prevPointOfView = mc.gameSettings.getPointOfView();
            rotation.x = LookHandler.getFreeYaw();
            rotation.y = LookHandler.getFreePitch();
        }
        if (keyDown && mc.currentScreen == null) {
            RotationComponent.update(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch), 360.0F, 1, 1);
            if (povMode.is("Сзади")) {
                mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
            } else {
                mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_FRONT);
            }
            pressed = true;
        } else if (pressed) {
            LookHandler.setFreeYaw(rotation.x);
            LookHandler.setFreePitch(rotation.y);
            mc.gameSettings.setPointOfView(prevPointOfView);
            pressed = false;
        }
    }

    @EventTarget
    public void onEvent(EventCancelThirdPerson eventThirdDistance) {
        if (keyDown) eventThirdDistance.setCancelled(true);
    }
}
