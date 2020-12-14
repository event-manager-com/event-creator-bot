package gregad.eventmanager.eventcreatorbot.bot;

import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import gregad.eventmanager.eventcreatorbot.service.user_service.UserService;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Greg Adler
 */

@Setter
public class Bot extends TelegramWebhookBot {
    private String botUserName;
    private String botToken;
    private String botWebHookUrl;
    
    @Autowired
    private TelegramFacade telegramFacade;
    
    public Bot(DefaultBotOptions options){
        super(options);
    }
    
    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        BotApiMethod replyMessageToUser = telegramFacade.handleUpdate(update);
        return replyMessageToUser;
    }

    @Override
    public String getBotPath() {
        return botWebHookUrl;
    }
}
