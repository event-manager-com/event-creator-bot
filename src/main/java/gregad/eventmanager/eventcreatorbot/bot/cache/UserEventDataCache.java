package gregad.eventmanager.eventcreatorbot.bot.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gregad.eventmanager.eventcreatorbot.bot.BotState;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Greg Adler
 */
@Component
public class UserEventDataCache implements DataCache {
    private Cache<Integer,BotState> sessionStates= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer,EventDto> eventStates= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, UserDto> users= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    private Cache<Integer, List<EventResponseDto>> userEvents= CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    @Override
    public void setUsersCurrentBotState(Integer userId, BotState botState) {
        sessionStates.put(userId, botState);
    }

    @Override
    public BotState getUsersCurrentBotState(Integer userId) {
        return sessionStates.getIfPresent(userId);
    }

    @SneakyThrows
    @Override
    public EventDto getUserEventData(Integer userId) {
        return eventStates.get(userId,EventDto::new);
    }

    @Override
    public void saveUserProfileData(Integer userId, EventDto userEventData) {
        eventStates.put(userId, userEventData);
    }

    @SneakyThrows
    @Override
    public UserDto getUserData(Integer userId) {
        return users.get(userId,null);
    }

    @Override
    public void SaveUserData(UserDto userDto) {
        users.put(userDto.getId(),userDto);
    }

    @SneakyThrows
    @Override
    public List<EventResponseDto> getUserEvents(Integer userId) {
        return userEvents.get(userId,null);
    }

    @Override
    public void setUserEvents(Integer userId, List<EventResponseDto> eventResponseDtoListvents) {
        userEvents.put(userId,eventResponseDtoListvents);
    }
}
