package su.hynix.component;

import com.darkmagician6.eventapi.EventManager;
import su.hynix.component.impl.MoveComponent;
import su.hynix.component.impl.RotationComponent;
import su.hynix.component.impl.SmoothRotationComponent;
import su.hynix.handlers.impl.LookHandler;

import java.util.HashMap;

public final class ComponentManager extends HashMap<Class<? extends Component>, Component> {

    public void init() {
        add(new LookHandler(), new RotationComponent(), new SmoothRotationComponent(), new MoveComponent());

        this.values().forEach(component -> EventManager.register(component));
    }

    public void add(Component... components) {
        for (Component component : components) {
            this.put(component.getClass(), component);
        }
    }


    public <T extends Component> T get(final Class<T> clazz) {
        return this.values()
                .stream()
                .filter(component -> component.getClass() == clazz)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
}