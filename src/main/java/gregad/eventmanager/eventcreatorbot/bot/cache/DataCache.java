package gregad.eventmanager.eventcreatorbot.bot.cache;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model.*;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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

    List<EventResponseDto> getUserEventsByFilter(Integer userId, String filter);

    void setUserEventsByFilter(Integer userId, String filter, List<EventResponseDto> userEvents);

    InlineKeyboardMarkup getCalendarKeyboard(String name);

    void setCalendarKeyboard(String name, InlineKeyboardMarkup keyboard);

    InlineKeyboardMarkup getImageTemplateKeyboard(String name);

    void setImageTemplateKeyboard(String name, InlineKeyboardMarkup keyboard);

    EventFilterDates getEventFilterDates(int userId);

    void setEventFilterDates(int userId, EventFilterDates eventFilterDates);

}
