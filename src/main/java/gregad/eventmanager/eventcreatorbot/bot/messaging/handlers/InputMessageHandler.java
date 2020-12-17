package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Greg Adler
 */
public interface InputMessageHandler {
    BotApiMethod<?> handle(Update update);
    BotState getHandlerName();
}
