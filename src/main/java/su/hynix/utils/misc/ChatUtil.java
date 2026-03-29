package su.hynix.utils.misc;

import net.minecraft.util.text.*;
import su.hynix.ui.gui.themes.ThemeEditor;
import su.hynix.ui.gui.themes.ThemeSettings;
import su.hynix.utils.Wrapper;
import su.hynix.utils.render.ColorUtil;

public class ChatUtil implements Wrapper {
    public static final char COLOR_CODE = '§';

    public static void addText(final Object message, final Object... objects) {
        if (mc.player == null) return;
        if (message == null) {
            addText("Object is null");
            return;
        }

        final String prefix = "Hynix Free ⇨";
        final int startColor = ThemeEditor.getColor(ThemeSettings.LOGO);
        final int endColor = ColorUtil.darken(startColor, 0.5f);
        IFormattableTextComponent finalText = new StringTextComponent("");

        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            StringTextComponent letter = new StringTextComponent(String.valueOf(c));
            Style style;

            if (c == '⇨') {
                style = Style.EMPTY.setColor(Color.fromInt(0x888888));
            } else {
                float progress = (float) i / (prefix.length() - 1);
                int gradientColor = ColorUtil.interpolate(startColor, endColor, progress);
                style = Style.EMPTY.setColor(Color.fromInt(gradientColor & 0xFFFFFF)).applyFormatting(TextFormatting.BOLD);
            }

            letter.setStyle(style);
            finalText.append(letter);
        }

        finalText.append(new StringTextComponent(" "));

        if (message instanceof ITextComponent) {
            finalText.append((ITextComponent) message);
        } else {
            String msg = String.format(message.toString(), objects).replace('&', COLOR_CODE);
            String cleanMsg = msg.replaceAll("§[0-9a-fk-or]", "");
            StringTextComponent mainText = new StringTextComponent(cleanMsg);
            mainText.setStyle(Style.EMPTY.setColor(Color.fromInt(0xFFFFFF)));
            finalText.append(mainText);
        }

        mc.ingameGUI.getChatGUI().printChatMessage(finalText);
    }
}
