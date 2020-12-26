package gregad.eventmanager.eventcreatorbot.bot;

import gregad.eventmanager.eventcreatorbot.api.ApiConstants;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants;
import lombok.Setter;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

/**
 * @author Greg Adler
 */

@Setter
public class Bot extends TelegramWebhookBot {
    private String botUserName;
    private String botToken;
    private String botWebHookUrl;
    

    private TelegramFacade telegramFacade;
    
    public Bot(DefaultBotOptions options, TelegramFacade telegramFacade){
        super(options);
        this.telegramFacade=telegramFacade;
    }
    
    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
//        SendPhoto sendPhoto = new SendPhoto();
//        sendPhoto.setChatId(update.hasCallbackQuery()?update.getCallbackQuery().getMessage().getChatId():update.getMessage().getChatId());
//        sendPhoto.setPhoto(new File(BotConstants.PATH_TO_TEMPLATES+"\\Other\\other.jpg"));
//        execute(sendPhoto);
        return telegramFacade.handleUpdate(update);
    }
    

    @Override
    public String getBotPath() {
        return botWebHookUrl;
    }
}
