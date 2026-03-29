package su.hynix.commands.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import su.hynix.commands.Command;
import su.hynix.events.EventClientClick;
import su.hynix.hynix;
import su.hynix.managers.impl.staffmanager.StaffManager;
import su.hynix.utils.Wrapper;
import su.hynix.utils.misc.ChatUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class StaffCommand extends Command implements ArgumentType<String>, Wrapper {

    public StaffCommand() {
        super("staff");
    }

    @Override
    public void run(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.executes(this::invalidArgs);

        builder.then(literal("add").then(args("player", new StaffCommand()).executes(context -> {
            String name = context.getArgument("player", String.class);
            boolean exists = hynix.getInstance().getStaffManager().isStaff(name);
            if (!exists) {
                hynix.getInstance().getStaffManager().addStaff(name);
            }
            IFormattableTextComponent prefix = new StringTextComponent("Персонал с именем '").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent(name).setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent("'" + (exists ? " уже существует!" : " добавлен!")).setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(args("player", new StaffCommand()).executes(context -> {
            String name = context.getArgument("player", String.class);
            boolean isStaff = hynix.getInstance().getStaffManager().isStaff(name);
            if (!isStaff) {
                return invalidArgs(context);
            }
            hynix.getInstance().getStaffManager().removeStaff(name);
            IFormattableTextComponent prefix = new StringTextComponent("Персонал с именем '").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent(name).setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent("' удалён!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("list").executes(context -> {
            List<StaffManager.StaffEntry> list = hynix.getInstance().getStaffManager().getStaff();
            if (list == null || list.isEmpty()) {
                IFormattableTextComponent emptyMsg = new StringTextComponent("Список персонала пуст!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
                ChatUtil.addText(emptyMsg);
                return SINGLE_SUCCESS;
            }
            IFormattableTextComponent title = new StringTextComponent("Список персонала:").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(title);
            for (StaffManager.StaffEntry e : list) {
                String staffName = e.name();
                IFormattableTextComponent staffText = new StringTextComponent(staffName).setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
                IFormattableTextComponent removeBtn = new StringTextComponent(" [Удалить]").setStyle(Style.EMPTY.applyFormatting(TextFormatting.RED).setClickEvent(new EventClientClick(ClickEvent.Action.RUN_COMMAND, hynix.getInstance().getCommandsManager().getPrefix() + getCommand() + " remove " + staffName)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Клик для удаления персонала").setStyle(Style.EMPTY.applyFormatting(TextFormatting.RED)))));
                ChatUtil.addText(new StringTextComponent("").append(staffText).append(removeBtn));
            }
            IFormattableTextComponent total = new StringTextComponent("Всего: " + list.size()).setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(total);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear").executes(context -> {
            List<StaffManager.StaffEntry> list = hynix.getInstance().getStaffManager().getStaff();
            if (list == null || list.isEmpty()) {
                IFormattableTextComponent emptyMsg = new StringTextComponent("Список персонала пуст!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
                ChatUtil.addText(emptyMsg);
                return SINGLE_SUCCESS;
            }
            hynix.getInstance().getStaffManager().clearStaff();
            IFormattableTextComponent clearedMsg = new StringTextComponent("Список персонала очищен!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(clearedMsg);
            return SINGLE_SUCCESS;
        }));
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String[] inputParts = context.getInput().split(" ");
        boolean isRemove = inputParts.length > 1 && "remove".equals(inputParts[1]);
        List<String> suggestions;
        if (isRemove) {
            suggestions = hynix.getInstance().getStaffManager().getStaff().stream().map(StaffManager.StaffEntry::name).collect(Collectors.toList());
        } else {
            suggestions = mc.getConnection().getPlayerInfoMap().stream().map(info -> info.getGameProfile().getName()).collect(Collectors.toList());
        }
        return ISuggestionProvider.suggest(suggestions, builder);
    }
}
