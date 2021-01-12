package gregad.eventmanager.eventcreatorbot.bot.messaging.utils;

import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.LocaleMessageService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * @author Greg Adler
 */
@Component
public class ReplyMessagesService {
    private LocaleMessageService localeMessageService;

    public ReplyMessagesService(LocaleMessageService messageService) {
        this.localeMessageService = messageService;
    }

    public SendMessage getReplyMessage(long chatId, String replyMessage) {
        return new SendMessage(chatId, localeMessageService.getMessage(replyMessage));
    }

    public SendMessage getReplyMessage(long chatId, String replyMessage, String suffix) {
        String message = localeMessageService.getMessage(replyMessage);
        return new SendMessage(chatId, message + "\n" + suffix);
    }

    public SendMessage getReplyMessage(long chatId, String replyMessage, Object... args) {
        return new SendMessage(chatId, localeMessageService.getMessage(replyMessage, args));
    }
}
