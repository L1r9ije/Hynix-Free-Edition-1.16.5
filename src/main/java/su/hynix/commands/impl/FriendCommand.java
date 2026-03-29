package su.hynix.commands.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
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
import su.hynix.managers.impl.FriendManager;
import su.hynix.utils.Wrapper;
import su.hynix.utils.misc.ChatUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendCommand extends Command implements ArgumentType<String>, Wrapper {

    public FriendCommand() {
        super("fr", "friend");
    }

    @Override
    public void run(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.executes(this::invalidArgs);

        builder.then(literal("add").then(args("player", new FriendCommand()).executes(context -> {
            String friendName = context.getArgument("player", String.class);
            boolean alreadyFriend = hynix.getInstance().getFriendManager().isFriend(friendName);
            IFormattableTextComponent prefix = new StringTextComponent("Друг с именем ").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent("\"" + friendName + "\"").setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent(alreadyFriend ? " уже существует!" : " добавлен!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));

            if (!alreadyFriend) {
                hynix.getInstance().getFriendManager().addFriend(friendName);
            }

            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(args("friend", new FriendCommand()).executes(context -> {
            String friendName = context.getArgument("friend", String.class);
            boolean isFriend = hynix.getInstance().getFriendManager().isFriend(friendName);

            if (!isFriend) return invalidArgs(context);

            hynix.getInstance().getFriendManager().removeFriend(friendName);
            IFormattableTextComponent prefix = new StringTextComponent("Друг с именем ").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent("\"" + friendName + "\"").setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent(" удалён!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));

            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("list").executes(context -> {
            List<FriendManager.FriendEntry> friends = hynix.getInstance().getFriendManager().getFriends();
            if (friends == null || friends.isEmpty()) {
                IFormattableTextComponent emptyMsg = new StringTextComponent("Список друзей пуст!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
                ChatUtil.addText(emptyMsg);
                return SINGLE_SUCCESS;
            }

            IFormattableTextComponent title = new StringTextComponent("Список друзей:").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(title);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            for (FriendManager.FriendEntry friend : friends) {
                String friendName = friend.name();
                IFormattableTextComponent friendText = new StringTextComponent(friendName).setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
                IFormattableTextComponent added = new StringTextComponent(" Добавлен: ").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
                IFormattableTextComponent date = new StringTextComponent(friend.addedTime().format(formatter)).setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
                IFormattableTextComponent removeBtn = new StringTextComponent(" [Удалить]").setStyle(Style.EMPTY.applyFormatting(TextFormatting.RED).setClickEvent(new EventClientClick(ClickEvent.Action.RUN_COMMAND, hynix.getInstance().getCommandsManager().getPrefix() + getCommand() + " remove " + friendName)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Клик для удаления" + friendName).setStyle(Style.EMPTY.applyFormatting(TextFormatting.RED)))));

                ChatUtil.addText(new StringTextComponent("").append(friendText).append(added).append(date).append(removeBtn));
            }

            IFormattableTextComponent total = new StringTextComponent("Всего: " + friends.size()).setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(total);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear").executes(context -> {
            hynix.getInstance().getFriendManager().clearFriends();
            IFormattableTextComponent clearedMsg = new StringTextComponent("Список друзей очищен!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(clearedMsg);
            return SINGLE_SUCCESS;
        }));
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readString();
        if ("remove".equals(reader.getString().split(" ")[1]) && (hynix.getInstance().getFriendManager() == null || !hynix.getInstance().getFriendManager().isFriend(input))) {
            throw new DynamicCommandExceptionType(name -> new StringTextComponent("Друга с именем " + name + " не существует")).create(input);
        }
        return input;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> suggestions;
        String[] inputParts = context.getInput().split(" ");
        boolean isRemoveCommand = inputParts.length > 1 && "remove".equals(inputParts[1]);

        if (isRemoveCommand) {
            suggestions = hynix.getInstance().getFriendManager().getFriends().stream()
                    .map(FriendManager.FriendEntry::name)
                    .collect(Collectors.toList());
        } else {
            suggestions = mc.getConnection().getPlayerInfoMap().stream()
                    .map(info -> info.getGameProfile().getName())
                    .collect(Collectors.toList());
        }

        return ISuggestionProvider.suggest(suggestions, builder);
    }
}