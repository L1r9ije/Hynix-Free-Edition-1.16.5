package su.hynix.modules.api.elements;

import lombok.Getter;
import lombok.Setter;
import su.hynix.ui.gui.Panel;
import su.hynix.utils.animation.AnimationUtil;
import su.hynix.utils.render.font.StyledFont;

@Getter
@Setter
public class Element implements IBuilder {
    private final AnimationUtil visibilityAnimation = new AnimationUtil(0.0f, 15.0f);
    private final StyledFont.TextScrollState scrollState = new StyledFont.TextScrollState();
    private float x, y, width, height;
    private Panel panel;

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isVisible() {
        return true;
    }

    public float getVisibilityAnimation() {
        visibilityAnimation.update(isVisible() ? 1.0f : 0.0f);
        return visibilityAnimation.getValue();
    }
}