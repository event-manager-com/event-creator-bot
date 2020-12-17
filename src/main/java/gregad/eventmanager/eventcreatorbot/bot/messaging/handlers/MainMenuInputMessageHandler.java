package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.MainMenu;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import gregad.eventmanager.eventcreatorbot.service.user_service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * @author Greg Adler
 */
@Component
public class MainMenuInputMessageHandler implements InputMessageHandler {
    private MainMenu mainMenu;
    private UserEventDataCache userEventDataCache;
    private EventService eventService;
    private UserService userService;

    @Autowired
    public MainMenuInputMessageHandler(UserEventDataCache userEventDataCache,
                                       EventService eventService,
                                       UserService userService,
                                       MainMenu mainMenu) {
        this.userEventDataCache = userEventDataCache;
        this.eventService = eventService;
        this.userService = userService;
        this.mainMenu=mainMenu;
    }

    @Override
    public BotApiMethod handle(Update update) {
        Long chatId;
        User from;
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            chatId = callbackQuery.getMessage().getChatId();
            from=callbackQuery.getFrom();
        }else {
            Message message = update.getMessage();
            chatId=message.getChatId();
            from=message.getFrom();
        }
        Integer id = from.getId();
        String firstName = from.getFirstName();
        String lastName = from.getLastName()==null?"":from.getLastName();
        userEventDataCache.SaveUserData(new UserDto(id,firstName+" "+lastName));
        return mainMenu.getMainMenuMessage(chatId,"Use menu for navigation");
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SHOW_MAIN_MENU;
    }
}
