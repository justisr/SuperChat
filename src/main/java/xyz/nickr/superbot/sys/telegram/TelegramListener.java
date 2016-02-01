package xyz.nickr.superbot.sys.telegram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.inline.send.InlineQueryResponse;
import pro.zackpollard.telegrambot.api.chat.inline.send.results.InlineQueryResult;
import pro.zackpollard.telegrambot.api.chat.inline.send.results.InlineQueryResultArticle;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.ParticipantJoinGroupChatEvent;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.inline.InlineResultChosenEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import xyz.nickr.superbot.SuperBotCommands;
import xyz.nickr.superbot.SuperBotController;
import xyz.nickr.superbot.cmd.util.ConvertCommand;
import xyz.nickr.superbot.cmd.util.ConvertCommand.Conversion;
import xyz.nickr.superbot.cmd.util.CurrencyCommand;
import xyz.nickr.superbot.sys.Group;
import xyz.nickr.superbot.sys.GroupConfiguration;
import xyz.nickr.superbot.sys.User;

/**
 * Created by bo0tzz
 */
public class TelegramListener implements Listener {

    private final TelegramBot bot;
    private final TelegramSys sys;
    private final Properties uids = new Properties();

    public TelegramListener(TelegramBot bot, TelegramSys sys) {
        this.bot = bot;
        this.sys = sys;

        try {
            uids.load(new FileInputStream(new File("tguids.cache")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        Group g = sys.wrap(event.getChat());
        User u = sys.wrap(event.getMessage().getSender());
        String msg = sys.prefix() + event.getCommand().trim() + " " + event.getArgsString().trim();
        SuperBotCommands.exec(sys, g, u, sys.wrap(msg, event.getMessage()));
    }

    @Override
    public void onParticipantJoinGroupChat(ParticipantJoinGroupChatEvent event) {
        User user = sys.wrap(event.getParticipant());
        Group convo = sys.wrap(event.getChat());
        GroupConfiguration cfg = SuperBotController.getGroupConfiguration(convo);
        if (cfg != null && cfg.isShowJoinMessage()) {
            String welcome = String.format(SuperBotController.WELCOME_MESSAGE_JOIN, user.getUsername(), convo.getDisplayName());
            String help = "You can access my help menu by typing `" + sys.prefix() + "help`";
            String message = sys.message()
                    .bold(true)
                    .text(TelegramMessageBuilder.markdown_escape(welcome, false) + "\n" + TelegramMessageBuilder.markdown_escape(help, false))
                    .bold(false)
                    .build();
            convo.sendMessage(message);
        }
    }

    private InlineQueryResultArticle res(String title, String desc, String text, boolean web) {
        return InlineQueryResultArticle.builder()
            .parseMode(ParseMode.MARKDOWN)
            .title(title)
            .description(desc)
            .disableWebPagePreview(!web)
            .messageText(text)
            .build();
    }

    @Override
    public void onInlineQueryReceived(InlineQueryReceivedEvent event) {
        String q = event.getQuery().getQuery().trim();
        System.out.println("INLINE QUERY RECEIVED: " + q);
        String[] words = q.split("\\s+");
        List<InlineQueryResult> results = new LinkedList<>();
        if (words.length >= 1) {
            if (words[0].equalsIgnoreCase("convert")) {
                // convert [from] [to] [quantity]
                if (words.length >= 4) {
                    String from = words[1], to = words[2], quant = words[3];
                    Map<String, Map<String, Conversion>> convs = ConvertCommand.conversions;
                    Map<String, Conversion> map = convs.get(from);
                    if (convs.containsKey(from) && map.containsKey(to)) {
                        String out = map.get(to).apply(quant);
                        String text = "[Convert] " + quant + " " + from + " => " + to + " = " + out;
                        results.add(res(from + " => " + to, quant + " => " + out, text, false));
                    }
                }
            } else if (words[0].equalsIgnoreCase("convert")) {
                // currency [from] [to] [quantity]
                if (words.length >= 4) {
                    String from = words[1], to = words[2], quant = words[3];
                    String queryURL = String.format("https://www.google.co.uk/finance/converter?from=%s&to=%s&a=%s", from, to, quant);
                    try {
                        URL url = new URL(queryURL);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        String line;
                        while ((line = reader.readLine()) != null && results.isEmpty()) {
                            Matcher matcher = CurrencyCommand.CONVERSION_PATTERN.matcher(line);
                            if (matcher.find()) {
                                String out = "[Currency] " + quant + " " + from + " => " + to + " = " + matcher.group(1);
                                results.add(res(from + " => " + to, quant + " => " + matcher.group(1), out, false));
                            }
                        }
                    } catch (IOException e) {}
                }
            }
        }
        if (results.isEmpty()) {
            String un = bot.getBotUsername();
            results.add(res("Convert", "@" + un + " convert [from] [to] [quantity]", "", false));
            results.add(res("Currency", "@" + un + " currency [from] [to] [quantity]", "", false));
        }
        InlineQueryResponse res = InlineQueryResponse.builder()
                                        .is_personal(false)
                                        .results(results)
                                        .cache_time(0)
                                        .build();
        event.getQuery().answer(bot, res);
    }

    @Override
    public void onInlineResultChosen(InlineResultChosenEvent event) {
    }

}