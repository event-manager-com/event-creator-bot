package gregad.eventmanager.eventcreatorbot.bot.cache;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.EventModel;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

/**
 * @author Greg Adler
 */
public interface DataCache {
    void setUsersCurrentBotState(Integer userId, BotState botState);
    BotState getUsersCurrentBotState(Integer userId);

    void setUsersCurrentBotStateStep(Integer userId, BotStateStep botStateStep);
    BotStateStep getUsersCurrentBotStateStep(Integer userId);
    
    EventModel getCurrentEventData(Integer userId);
    void saveCurrentEventData(Integer userId, EventModel userEventData);
    
    UserDto getUserData(Integer userId);
    void SaveUserData(UserDto userDto);
    
    List<EventResponseDto> getUserEvents(Integer userId);
    void setUserEvents(Integer userId,List<EventResponseDto> userEvents);
    
    ReplyKeyboard getCalendarKeyboard(String name);
    void setCalendarKeyboard(String name, ReplyKeyboard keyboard);
}
