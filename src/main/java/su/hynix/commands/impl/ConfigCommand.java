package su.hynix.commands.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import su.hynix.managers.FilePath;
import su.hynix.utils.misc.ChatUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ConfigCommand extends Command implements ArgumentType<String> {

    public ConfigCommand() {
        super("config", "cfg");
    }

    @Override
    public void run(LiteralArgumentBuilder<ISuggestionProvider> builder) {
        builder.executes(this::invalidArgs);

        builder.then(literal("save").then(args("name", StringArgumentType.word()).executes(context -> {
            String configName = context.getArgument("name", String.class);
            hynix.getInstance().getConfigManager().saveConfig(configName);

            IFormattableTextComponent prefix = new StringTextComponent("Конфиг с именем ").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent("\"" + configName + "\"").setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent(" сохранён!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));

            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("load").then(args("name", new ConfigCommand()).executes(context -> {
            String configName = context.getArgument("name", String.class);
            if (!hynix.getInstance().getConfigManager().getConfigNames().contains(configName))
                return invalidArgs(context);
            hynix.getInstance().getConfigManager().loadConfig(configName);

            IFormattableTextComponent prefix = new StringTextComponent("Конфиг с именем ").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent("\"" + configName + "\"").setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent(" загружён!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));

            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("remove").then(args("name", new ConfigCommand()).executes(context -> {
            String configName = context.getArgument("name", String.class);
            if (!hynix.getInstance().getConfigManager().getConfigNames().contains(configName))
                return invalidArgs(context);
            hynix.getInstance().getConfigManager().deleteConfig(configName);

            IFormattableTextComponent prefix = new StringTextComponent("Конфиг с именем ").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            IFormattableTextComponent nameText = new StringTextComponent("\"" + configName + "\"").setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
            IFormattableTextComponent suffix = new StringTextComponent(" удалён!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));

            ChatUtil.addText(new StringTextComponent("").append(prefix).append(nameText).append(suffix));
            return SINGLE_SUCCESS;
        })));

        builder.then(literal("list").executes(context -> {
            List<String> configList = hynix.getInstance().getConfigManager().getConfigNames();
            if (configList == null || configList.isEmpty()) {
                IFormattableTextComponent emptyMsg = new StringTextComponent("Список конфигураций пуст!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
                ChatUtil.addText(emptyMsg);
                return SINGLE_SUCCESS;
            }

            IFormattableTextComponent title = new StringTextComponent("Список конфигов:").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(title);

            for (String cfgName : configList) {
                IFormattableTextComponent configText = new StringTextComponent(cfgName).setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));

                IFormattableTextComponent loadBtn = new StringTextComponent(" [Загрузить]").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GREEN).setClickEvent(new EventClientClick(ClickEvent.Action.RUN_COMMAND, hynix.getInstance().getCommandsManager().getPrefix() + getCommand() + " load " + cfgName)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Загрузить конфиг " + cfgName))));

                IFormattableTextComponent removeBtn = new StringTextComponent(" [Удалить]").setStyle(Style.EMPTY.applyFormatting(TextFormatting.RED).setClickEvent(new EventClientClick(ClickEvent.Action.RUN_COMMAND, hynix.getInstance().getCommandsManager().getPrefix() + getCommand() + " remove " + cfgName)).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Удалить конфиг " + cfgName))));

                ChatUtil.addText(new StringTextComponent("").append(configText).append(loadBtn).append(removeBtn));
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("dir").executes(context -> {
            try {
                File configDirectory = new File(FilePath.BASE_PATH + FilePath.CUSTOM_DIR_REL);
                Runtime.getRuntime().exec("explorer " + configDirectory.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("clear").executes(context -> {
            List<String> configs = hynix.getInstance().getConfigManager().getConfigNames();
            configs.forEach(config -> hynix.getInstance().getConfigManager().deleteConfig(config));
            IFormattableTextComponent clearedMsg = new StringTextComponent("Список конфигов очищен!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(clearedMsg);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("reset").executes(context -> {
            hynix.getInstance().getConfigManager().resetToDefaults();
            IFormattableTextComponent resetMsg = new StringTextComponent("Конфиг сброшен!").setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
            ChatUtil.addText(resetMsg);
            return SINGLE_SUCCESS;
        }));
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String config = reader.readString();
        boolean exists = hynix.getInstance().getConfigManager().getConfigNames().contains(config);
        if (!exists) {
            throw new DynamicCommandExceptionType(name -> new StringTextComponent("Конфиг с именем " + name + " не существует")).create(config);
        }
        return config;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> configNames = hynix.getInstance().getConfigManager().getConfigNames();
        return ISuggestionProvider.suggest(configNames, builder);
    }
}
