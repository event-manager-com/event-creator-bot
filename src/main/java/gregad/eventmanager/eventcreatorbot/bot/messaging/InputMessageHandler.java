package gregad.eventmanager.eventcreatorbot.bot.messaging;

import gregad.eventmanager.eventcreatorbot.bot.BotState;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * @author Greg Adler
 */
public interface InputMessageHandler {
    BotApiMethod<?> handle(Message message);
    BotState getHandlerName();
}
