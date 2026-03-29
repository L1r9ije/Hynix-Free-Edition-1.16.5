package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import su.hynix.events.EventPacket;
import su.hynix.modules.Category;
import su.hynix.modules.Module;

public class SRPSpoofer extends Module {

    public SRPSpoofer() {
        super("SRPSpoof", "Подменяет данные о том, что установлен серверный ресурс-пак", Category.Miscellaneous);
    }

    @EventTarget
    public void onPacketReceive(EventPacket e) {
        if ((e.getPacket() instanceof SSendResourcePackPacket) && e.isReceive()) {
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
            e.setCancelled(true);
        }
    }
}