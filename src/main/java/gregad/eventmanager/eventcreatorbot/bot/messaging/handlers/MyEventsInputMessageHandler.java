package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;

/**
 * @author Greg Adler
 */
@Component
public class MyEventsInputMessageHandler implements InputMessageHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        return null;
    }

    @Override
    public BotState getHandlerName() {
        return GET_EVENTS;
    }
}
