package su.hynix.ui.Interface.elements;


import su.hynix.events.EventRender2D;
import su.hynix.utils.Wrapper;

public interface ElementRender extends Wrapper {
    void render(EventRender2D.Post eventRender);
}
