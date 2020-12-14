package gregad.eventmanager.eventcreatorbot.bot.messaging;

import gregad.eventmanager.eventcreatorbot.bot.BotState;
import gregad.eventmanager.eventcreatorbot.bot.MainMenu;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import gregad.eventmanager.eventcreatorbot.service.user_service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * @author Greg Adler
 */
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
    public BotApiMethod handle(Message message) {
        System.out.println("in menu ");//////////////////////////
        return mainMenu.getMainMenuMessage(message.getChatId(),message.getText());
    }

    @Override
    public BotState getHandlerName() {
        return BotState.SHOW_MAIN_MENU;
    }
}
