package gregad.eventmanager.eventcreatorbot.bot;

import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;

/**
 * @author Greg Adler
 */
@Component
public class TelegramFacade {
    private BotStateContext botStateContext;
    private UserEventDataCache userDataCache;
    private MainMenu mainMenu;

    public TelegramFacade(BotStateContext botStateContext, UserEventDataCache userDataCache, MainMenu mainMenu) {
        this.botStateContext = botStateContext;
        this.userDataCache = userDataCache;
        this.mainMenu = mainMenu;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        BotApiMethod<?> replyMessage;
        replyMessage = handleInputMessage(update);
        return replyMessage;
    }

    private BotApiMethod<?> handleInputMessage(Update update) {
        String inputMsg;
        int userId;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            inputMsg = callbackQuery.getData();
            userId = callbackQuery.getFrom().getId();
        } else {
            inputMsg = update.getMessage().getText();
            userId = update.getMessage().getFrom().getId();
        }
        BotState botState;
        BotApiMethod replyMessage;
        if (inputMsg == null) {
            return botStateContext.processInputMessage(SHOW_MAIN_MENU, update);
        }
        switch (inputMsg) {
            case "/start":
                botState = SHOW_MAIN_MENU;
                userDataCache.setUsersCurrentBotStateStep(userId, BotStateStep.NO_STATE_STEP);
                break;
            case "New Event":
                botState = FILLING_EVENT_FORM;
                userDataCache.setUsersCurrentBotStateStep(userId, BotStateStep.NO_STATE_STEP);
                break;
            case "My Events":
                botState = GET_EVENTS;
                userDataCache.setUsersCurrentBotStateStep(userId, BotStateStep.NO_STATE_STEP);
                break;
            case "Step Back":
                botState = STEP_BACK;
                break;
            case "Help":
                botState = BotState.SHOW_HELP_MENU;
                userDataCache.setUsersCurrentBotStateStep(userId, BotStateStep.NO_STATE_STEP);
                break;
            default:
                botState = userDataCache.getUsersCurrentBotState(userId);
                break;
        }
//        System.out.println(inputMsg);
//        System.out.println(botState);
        userDataCache.setUsersCurrentBotState(userId, botState);

        replyMessage = botStateContext.processInputMessage(botState, update);

        return replyMessage;
    }

}
