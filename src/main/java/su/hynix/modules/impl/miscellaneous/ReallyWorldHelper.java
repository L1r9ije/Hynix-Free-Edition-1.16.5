package su.hynix.modules.impl.miscellaneous;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import su.hynix.events.EventPacket;
import su.hynix.modules.Category;
import su.hynix.modules.Module;
import su.hynix.modules.api.constructors.impl.BooleanSetting;
import su.hynix.utils.misc.ChatUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReallyWorldHelper extends Module {

    private final BooleanSetting close = new BooleanSetting("Закрывать меню", false);
    private final BooleanSetting filter = new BooleanSetting("Фильтр чата", false);

    private final Set<String> bannedWords = new HashSet<>(Arrays.asList(
            "акриен(а|у|ом|е|чик)?", "рич(а|у|ом|ей|е)?", "ньюкод(ом|а|у|ами|ик|е)?",
            "экспенсив(ом|а|у|ами|е)?", "импакт(ом|а|у|ами|ик|е)?", "экселлент(ом|а|у|ами|ик|е)?",
            "экселент(ом|а|у|ами|ик)?", "катлаван(ом|а|у|ами|чик)?", "катлован(ом|а|у|ами|чик)?",
            "целестиал(ом|а|у|ами|е)?", "целк(ой|а|у|ами|очка|е)?", "матикс(ом|а|у|ами|е)?",
            "инерти(я|ей|ю|ями|е)?", "эксп(а|ой|ою|у|уличка|е)?", "флюгер(ом|а|у|ами)?",
            "рикер(а|у|ом|очек)?", "фанпе(й|ю|я|ем|е|йчик)?", "вексайд(ом|а|у|ами|ик|е)?",
            "нурсултан(а|у|е|ом|чик)?", "нурик(а|у|ом|е)?", "нурлан(а|у|ом|чик|е)?",
            "векс(ом|у|а|ами|ик|е)?", "релейк(ом|у|а|ами|е)?", "арбуз(ом|а|у|ами|ик|е)?",
            "вилд(ом|у|а|ами|ик|е)?", "фантайм(е|а|у)?", "холик(е|а|у)?",
            "холиворлд(а|у|е)?", "рокстар(ом|а|у|ами|чик|е)?", "рогалик(а|у|ом|е)?",
            "тандерхак(ом|у|и|ами|а|е)?", "ликвидбаунс(а|у|ами|е)?",
            "expensive", "celestial", "newcode", "arbuz", "akrien", "nursultan", "relake", "wild", "wurst", "catlovan",
            "excellent", "rockstar", "catlavan", "impact", "matix", "inertia", "wex", "wexside", "nurik", "nurlan",
            "rich", "funpay", "fluger", "riker", "funtime", "holyworld", "wwe", "hvh", "rogalik", "thunderhack",
            "liquidbounce"
    ));


    public ReallyWorldHelper() {
        super("Really World Helper", "Помощник для сервера ReallyWorld", Category.Miscellaneous);
        addSettings(close, filter);
    }


    @EventTarget
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SOpenWindowPacket packet && close.get()) {
            if (packet.getTitle().getString().contains("ꈁꀀꈂꌁꈂꀁ§0ꈃꄀ") && mc.player.ticksExisted < 100) {
                mc.player.connection.sendPacket(new CCloseWindowPacket(packet.getWindowId()));
                e.setCancelled(true);
            }
        }
        if (e.getPacket() instanceof CChatMessagePacket && filter.get()) {
            String message = ((CChatMessagePacket) e.getPacket()).getMessage().toLowerCase();
            boolean banwords = false;
            for (String pattern : bannedWords) {
                if (message.matches(".*" + pattern + ".*")) {
                    banwords = true;
                    break;
                }
            }
            if (banwords) {
                e.setCancelled(true);
                ChatUtil.addText("В вашем сообщении было найдено запретное слово, отправка сообщения отменена!");
            }
        }
    }
}
