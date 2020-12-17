package gregad.eventmanager.eventcreatorbot.bot;

import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Greg Adler
 */
@Component
public class TelegramFacade {
    private BotStateContext botStateContext;
    private UserEventDataCache userDataCache;
    private MainMenu mainMenu;
    private Bot bot;

    public TelegramFacade(@Lazy Bot bot, BotStateContext botStateContext, UserEventDataCache userDataCache, MainMenu mainMenu) {
        this.botStateContext = botStateContext;
        this.userDataCache = userDataCache;
        this.mainMenu=mainMenu;
        this.bot=bot;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        BotApiMethod<?> replyMessage = null;
        replyMessage = handleInputMessage(update);
        return replyMessage;
    }

    private BotApiMethod<?> handleInputMessage(Update update) {
        String inputMsg;
        int userId;
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            inputMsg=callbackQuery.getData();
            userId=callbackQuery.getFrom().getId();
        }else {
            inputMsg = update.getMessage().getText();
            userId = update.getMessage().getFrom().getId();
        }
        BotState botState;
        BotApiMethod replyMessage;
        switch (inputMsg) {
            case "/start":
                botState = BotState.SHOW_MAIN_MENU;
                break;
            case "New Event":
                botState=BotState.FILLING_EVENT_FORM;
                break;
            case "My Events":
                botState = BotState.GET_EVENTS;
                break;
            case "Step Back":
                botState = BotState.STEP_BACK;
                System.out.println(botState);
                break;
            case "Help":
                botState = BotState.SHOW_HELP_MENU;
                break;
            default:
                botState = userDataCache.getUsersCurrentBotState(userId);
                break;
        }
        System.out.println(inputMsg);
        System.out.println(botState);
        userDataCache.setUsersCurrentBotState(userId, botState);

        replyMessage = botStateContext.processInputMessage(botState, update);

        return replyMessage;
    }
    
}
