package gregad.eventmanager.eventcreatorbot.bot.cache;

import gregad.eventmanager.eventcreatorbot.bot.BotState;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;

import java.util.List;

/**
 * @author Greg Adler
 */
public interface DataCache {
    void setUsersCurrentBotState(Integer userId, BotState botState);
    BotState getUsersCurrentBotState(Integer userId);
    
    EventDto getUserEventData(Integer userId);
    void saveUserProfileData(Integer userId, EventDto userEventData);
    
    UserDto getUserData(Integer userId);
    void SaveUserData(UserDto userDto);
    
    List<EventResponseDto> getUserEvents(Integer userId);
    
    void setUserEvents(Integer userId,List<EventResponseDto> userEvents);
}
