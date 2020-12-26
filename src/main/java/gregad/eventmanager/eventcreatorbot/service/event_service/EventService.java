package gregad.eventmanager.eventcreatorbot.service.event_service;

import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Greg Adler
 */
public interface EventService {
    EventResponseDto createEvent(EventDto event);
    EventResponseDto updateEvent(int ownerId, EventDto event);
    EventResponseDto deleteEvent(int ownerId, long eventId);
    EventResponseDto getEventById(int ownerId, long eventId);
    List<EventResponseDto> getEventByTitle(int ownerId, String title);
    List<EventResponseDto> getFutureEvents(int ownerId);
    List<EventResponseDto> getEventsByDate(int ownerId, LocalDate from, LocalDate to);
    List<EventResponseDto> getEventsByInvitedUser(int ownerId, String userInvited);
}
