package gregad.eventmanager.eventcreatorbot.bot;

import gregad.eventmanager.eventcreatorbot.bot.messaging.InputMessageHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Greg Adler
 */
@Component
public class BotStateContext {
    private ConcurrentHashMap<BotState, InputMessageHandler> messageHandlers = new ConcurrentHashMap<>();

    public BotStateContext(List<InputMessageHandler> messageHandlers) {
        messageHandlers.forEach(handler -> this.messageHandlers.put(handler.getHandlerName(), handler));
    }

    public BotApiMethod processInputMessage(BotState currentState, Message message) {
        InputMessageHandler currentMessageHandler = findMessageHandler(currentState);
        return currentMessageHandler.handle(message);
    }

    private InputMessageHandler findMessageHandler(BotState currentState) {
        if (isFillingProfileState(currentState)) {
            return messageHandlers.get(BotState.FILLING_EVENT_FORM);
        }

        return messageHandlers.get(currentState);
    }

    private boolean isFillingProfileState(BotState currentState) {
        switch (currentState) {
            case ASK_TITLE:
            case ASK_DESCRIPTION:
            case ASK_DATE:
            case ASK_TIME:
            case ASK_TEMPLATE:
            case FILLING_EVENT_FORM:
            case EVENT_FILLED:
                return true;
            default:
                return false;
        }
    }
}
