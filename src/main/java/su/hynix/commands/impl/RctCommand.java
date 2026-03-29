package su.hynix.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import su.hynix.commands.Command;
import su.hynix.handlers.impl.ReallyWorldJoinHandler;
import su.hynix.utils.Wrapper;
import su.hynix.utils.misc.ChatUtil;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class RctCommand extends Command implements Wrapper {


    public RctCommand() {
        super("rct");
    }

    @Override
    public void run(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.executes(ctx -> {
            if (ReallyWorldJoinHandler.getGrief() == -1) {
                ChatUtil.addText(new StringTextComponent("Гриф не найден!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY)));
            } else {
                ReallyWorldJoinHandler.clearManualGrief();
                ReallyWorldJoinHandler.startRejoin();
            }
            return SINGLE_SUCCESS;
        });
    }
}
