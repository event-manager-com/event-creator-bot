package gregad.eventmanager.eventcreatorbot.bot;

import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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
        this.mainMenu=mainMenu;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        BotApiMethod replyMessage = null;

//        if (update.hasCallbackQuery()) {
//            CallbackQuery callbackQuery = update.getCallbackQuery();
//            return processCallbackQuery(callbackQuery);
//        }

        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            replyMessage = handleInputMessage(message);
        }

        return replyMessage;
    }

    private BotApiMethod<Message> handleInputMessage(Message message) {
        
        String inputMsg = message.getText();
        int userId = message.getFrom().getId();
        BotState botState;
        BotApiMethod replyMessage;

        switch (inputMsg) {
            case "/start":
                botState = BotState.SHOW_MAIN_MENU;
                break;
            case "Create new Event":
                botState = BotState.ASK_TITLE;
                break;
            case "My Events":
                botState = BotState.GET_EVENTS;
                break;    
            case "Help":
                botState = BotState.SHOW_HELP_MENU;
                break;
            default:
                botState = userDataCache.getUsersCurrentBotState(userId);
                break;
        }

        userDataCache.setUsersCurrentBotState(userId, botState);

        replyMessage = botStateContext.processInputMessage(botState, message);

        return replyMessage;
    }

//    private BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
//        final long chatId = buttonQuery.getMessage().getChatId();
//        final int userId = buttonQuery.getFrom().getId();
//        BotApiMethod<?> callBackAnswer = mainMenu.getMainMenuMessage(chatId, "Воспользуйтесь главным меню");
//
//
//        //From Destiny choose buttons
//        if (buttonQuery.getData().equals("buttonYes")) {
//            callBackAnswer = new SendMessage(chatId, "Как тебя зовут ?");
//            userDataCache.setUsersCurrentBotState(userId, BotState.ASK_AGE);
//        } else if (buttonQuery.getData().equals("buttonNo")) {
//            callBackAnswer = sendAnswerCallbackQuery("Возвращайся, когда будешь готов", false, buttonQuery);
//        } else if (buttonQuery.getData().equals("buttonIwillThink")) {
//            callBackAnswer = sendAnswerCallbackQuery("Данная кнопка не поддерживается", true, buttonQuery);
//        }
//
//        //From Gender choose buttons
//        else if (buttonQuery.getData().equals("buttonMan")) {
//            UserProfileData userProfileData = userDataCache.getUserProfileData(userId);
//            userProfileData.setGender("М");
//            userDataCache.saveUserProfileData(userId, userProfileData);
//            userDataCache.setUsersCurrentBotState(userId, BotState.ASK_COLOR);
//            callBackAnswer = new SendMessage(chatId, "Твоя любимая цифра");
//        } else if (buttonQuery.getData().equals("buttonWoman")) {
//            UserProfileData userProfileData = userDataCache.getUserProfileData(userId);
//            userProfileData.setGender("Ж");
//            userDataCache.saveUserProfileData(userId, userProfileData);
//            userDataCache.setUsersCurrentBotState(userId, BotState.ASK_COLOR);
//            callBackAnswer = new SendMessage(chatId, "Твоя любимая цифра");
//
//        } else {
//            userDataCache.setUsersCurrentBotState(userId, BotState.SHOW_MAIN_MENU);
//        }
//
//
//        return callBackAnswer;
//
//
//    }
}
