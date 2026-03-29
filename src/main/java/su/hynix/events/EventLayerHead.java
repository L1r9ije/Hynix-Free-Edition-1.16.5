package su.hynix.events;

import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.callables.EventCancellable;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;


@Data
@AllArgsConstructor
public class EventLayerHead extends EventCancellable implements Event {
    private LivingEntity entity;
    private MatrixStack matrix;
    private EntityModel<?> model;
}
