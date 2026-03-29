package su.hynix.utils.player;

import net.minecraft.util.text.TextFormatting;
import su.hynix.utils.Wrapper;

public class PotionUtil implements Wrapper {

    public static String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static TextFormatting getEffectColor(String effectName) {
        effectName = effectName.toLowerCase();
        if (effectName.contains("strength")) {
            return TextFormatting.RED;
        } else if (effectName.contains("fire_resistance")) {
            return TextFormatting.GOLD;
        } else if (effectName.contains("speed")) {
            return TextFormatting.AQUA;
        } else if (effectName.contains("absorption")) {
            return TextFormatting.YELLOW;
        } else if (effectName.contains("regeneration")) {
            return TextFormatting.LIGHT_PURPLE;
        } else if (effectName.contains("jump_boost")) {
            return TextFormatting.GREEN;
        }
        return TextFormatting.WHITE;
    }
}