package gregad.eventmanager.eventcreatorbot.bot.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.EventModel;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayList;
import java.util.List;
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
    private Cache<Integer, List<EventResponseDto>> userEvents= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<String, ReplyKeyboard> calendarKeyboardsCache= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    
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
    public List<EventResponseDto> getUserEvents(Integer userId) {
        return userEvents.get(userId,()->new ArrayList<EventResponseDto>());
    }

    @Override
    public void setUserEvents(Integer userId, List<EventResponseDto> eventResponseDtoListvents) {
        userEvents.put(userId,eventResponseDtoListvents);
    }

    @SneakyThrows
    @Override
    public ReplyKeyboard getCalendarKeyboard(String name) {
        return calendarKeyboardsCache.get(name,()->null);
    }

    @Override
    public void setCalendarKeyboard(String name, ReplyKeyboard keyboard) {
        calendarKeyboardsCache.put(name, keyboard);
    }


}
