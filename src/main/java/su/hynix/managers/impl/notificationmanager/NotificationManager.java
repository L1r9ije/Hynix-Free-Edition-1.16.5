package su.hynix.managers.impl.notificationmanager;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import su.hynix.events.EventRender2D;
import su.hynix.hynix;
import su.hynix.managers.impl.notificationmanager.impl.FontNotification;
import su.hynix.managers.impl.notificationmanager.impl.ImageNotification;
import su.hynix.modules.impl.visuals.Interface;
import su.hynix.utils.Wrapper;
import su.hynix.utils.render.font.Fonts;

import java.util.LinkedList;
import java.util.List;

public class NotificationManager implements Wrapper {
    private static final List<AbstractNotification> notifications = new LinkedList<>();

    private static void addNotification(AbstractNotification notification) {
        notification.getYAnimation().setValue(mw.getScaledHeight() / 2f + 21);
        notification.getYAnimation().update(13.5f);

        notifications.add(0, notification);

        if (notifications.size() > 10) {
            for (int i = 10; i < notifications.size(); i++) {
                notifications.get(i).expireNow();
            }
        }
    }

    public static boolean hasActiveNotifications() {
        return !notifications.isEmpty();
    }

    public static void addNotification(ResourceLocation image, ITextComponent message) {
        addNotification(new ImageNotification(image, message));
    }

    public static void addNotification(ResourceLocation image, ITextComponent message, int color) {
        addNotification(new ImageNotification(image, message, color));
    }

    public static void addNotification(String title, String message, int color) {
        addNotification(new FontNotification(title, message, color));
    }

    @EventTarget
    public void render(EventRender2D.Pre event) {
        float startY = mw.getScaledHeight() / 2f + 21;

        notifications.removeIf(n -> n.isExpired() && n.getAlphaAnimation().isDone());

        for (AbstractNotification notification : notifications) {
            float width;
            if (notification instanceof ImageNotification) {
                width = Fonts.sf_regular[12].getWidth(notification.getMessage().getString()) + 24;
            } else {
                width = Fonts.sf_regular[12].getWidth(notification.getMessage().getString()) + 10f;
            }

            float x = (mw.getScaledWidth() - width) / 2f;
            if (hynix.getInstance().getModuleManager().getModule(Interface.class).isEnabled()
                    && Interface.elements.is("Уведомления")) {
                notification.render(x, startY, event.getStack());
            }
            startY += 13.5f;
        }
    }
}