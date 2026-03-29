package su.hynix.modules;

import com.darkmagician6.eventapi.EventManager;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import su.hynix.managers.impl.notificationmanager.NotificationManager;
import su.hynix.modules.api.constructors.Setting;
import su.hynix.modules.impl.miscellaneous.ToggleSounds;
import su.hynix.ui.Interface.elements.impl.NotificationRender;
import su.hynix.utils.Wrapper;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.animation.Easings;
import su.hynix.utils.misc.SoundUtil;

import java.util.List;

@Getter
@Setter
public abstract class Module implements Wrapper {
    @Getter
    @Setter
    private static boolean suppressToggleEffects = false;
    private final AnimationUtil animation = new AnimationUtil(0.0f, 8f, Easings.LINEAR);
    String name;
    String description;
    Category category;
    int bind;
    boolean enabled;
    boolean premium;
    List<Setting<?>> settings = new ObjectArrayList<>();
    private ToggleMode toggleMode = ToggleMode.TOGGLE;
    private boolean keybindvisible = true;

    public Module(String name, Category category) {
        this(name, "NULL", false, category);
    }

    public Module(String name, String description, Category category) {
        this(name, description, false, category);
    }

    public Module(String name, String description, boolean premium, Category category) {
        this.name = name;
        this.description = description;
        this.premium = premium;
        this.category = category;
        this.bind = -100;
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public void onEnable() {
        animation.update(1f);
        if (!suppressToggleEffects && keybindvisible) {
            SoundUtil.playSound(ToggleSounds.getSoundFile(true));
            if (NotificationRender.module.get())
                NotificationManager.addNotification("a", name + " Включен", -1);
        }
        EventManager.register(this);
    }

    public void onDisable() {
        animation.update(0f);
        if (!suppressToggleEffects && keybindvisible) {
            SoundUtil.playSound(ToggleSounds.getSoundFile(false));
            if (NotificationRender.module.get())
                NotificationManager.addNotification("a", name + " Выключен", -1);
        }
        EventManager.unregister(this);
    }

    public final void toggle() {
        settoggled(!enabled);
    }

    public final void settoggled(boolean toggle) {
        if (enabled == toggle) {
            return;
        }
        enabled = toggle;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public enum ToggleMode {
        TOGGLE, HOLD
    }
}