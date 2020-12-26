package gregad.eventmanager.eventcreatorbot.bot.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model.*;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Greg Adler
 */
@Component
public class UserEventDataCache implements DataCache {
    private Cache<Integer,BotState> sessionStates= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, BotStateStep> sessionStateSteps= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, EventModel> eventStates= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, UserDto> users= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, Map<String,List<EventResponseDto>>> userEvents= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<String, InlineKeyboardMarkup> calendarKeyboardsCache= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<String, InlineKeyboardMarkup> templateKeyboardsCache= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, EventFilterDates> userEventFilterDates= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();


    @Override
    public void setUsersCurrentBotState(Integer userId, BotState botState) {
        sessionStates.put(userId, botState);
    }

    @SneakyThrows
    @Override
    public BotState getUsersCurrentBotState(Integer userId) {
        return sessionStates.get(userId,()->{
            setUsersCurrentBotState(userId,BotState.SHOW_MAIN_MENU);
            return BotState.SHOW_MAIN_MENU;});
    }

    @Override
    public void setUsersCurrentBotStateStep(Integer userId, BotStateStep botStateStep) {
        sessionStateSteps.put(userId,botStateStep);
    }

    @SneakyThrows
    @Override
    public BotStateStep getUsersCurrentBotStateStep(Integer userId) {
        return sessionStateSteps.get(userId,()->BotStateStep.NO_STATE_STEP);
    }

    @SneakyThrows
    @Override
    public EventModel getCurrentEventData(Integer userId) {
        return eventStates.get(userId,EventModel::new);
    }

    @Override
    public void saveCurrentEventData(Integer userId, EventModel userEventData) {
        eventStates.put(userId, userEventData);
    }

    @SneakyThrows
    @Override
    public UserDto getUserData(Integer userId) {
        return users.get(userId,UserDto::new);
    }

    @Override
    public void SaveUserData(UserDto userDto) {
        users.put(userDto.getId(),userDto);
    }

    @SneakyThrows
    @Override
    public List<EventResponseDto> getUserEventsByFilter(Integer userId, String filter) {
        Map<String, List<EventResponseDto>> stringListMap = userEvents.get(userId, () -> new HashMap<>());
        return stringListMap.getOrDefault(filter,new ArrayList<>());
    }

    @SneakyThrows
    @Override
    public void setUserEventsByFilter(Integer userId, String filter , List<EventResponseDto> eventResponseDtoListEvents) {
        userEvents.get(userId,()->new HashMap<>()).put(filter,eventResponseDtoListEvents);
    }

    @SneakyThrows
    @Override
    public InlineKeyboardMarkup getCalendarKeyboard(String name) {
        return calendarKeyboardsCache.get(name, ()->new InlineKeyboardMarkup().setKeyboard(new ArrayList<>(new ArrayList<>())));
    }

    @Override
    public void setCalendarKeyboard(String name, InlineKeyboardMarkup keyboard) {
        calendarKeyboardsCache.put(name, keyboard);
    }

    @SneakyThrows
    @Override
    public InlineKeyboardMarkup getImageTemplateKeyboard(String name) {
        return templateKeyboardsCache.get(name,()->new InlineKeyboardMarkup().setKeyboard(new ArrayList<>(new ArrayList<>())));
    }

    @Override
    public void setImageTemplateKeyboard(String name, InlineKeyboardMarkup keyboard) {
        templateKeyboardsCache.put(name,keyboard);
    }

    @SneakyThrows
    @Override
    public EventFilterDates getEventFilterDates(int userId) {
        return userEventFilterDates.get(userId,EventFilterDates::new);
    }

    @Override
    public void setEventFilterDates(int userId, EventFilterDates eventFilterDates) {
        userEventFilterDates.put(userId,eventFilterDates);
    }


}
