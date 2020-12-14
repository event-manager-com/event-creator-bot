package gregad.eventmanager.eventcreatorbot.service.event_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import gregad.eventmanager.eventcreatorbot.service.token_service.TokenHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.api.ApiConstants.HEADER;

/**
 * @author Greg Adler
 */
@Component
public class EventServiceImpl implements EventService {
    
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private TokenHolderService tokenHolderService;

    public EventServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, TokenHolderService tokenHolderService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenHolderService = tokenHolderService;
    }

    @Value("${event.service.url}")
    private String eventServiceUrl;
    
    @Override
    public EventResponseDto createEvent(EventDto event) {
        return null;
    }
    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        return httpHeaders;
    }

    @Override
    public EventResponseDto updateEvent(int ownerId, EventDto event) {
        return null;
    }

    @Override
    public EventResponseDto deleteEvent(int ownerId, long eventId) {
        return null;
    }

    @Override
    public EventResponseDto getEventById(int ownerId, long eventId) {
        return null;
    }

    @Override
    public List<EventResponseDto> getEventByTitle(int ownerId, String title) {
        return null;
    }

    @Override
    public List<EventResponseDto> getFutureEvents(int ownerId) {
        return null;
    }

    @Override
    public List<EventResponseDto> getEventsByDate(int ownerId, LocalDate from, LocalDate to) {
        return null;
    }

    @Override
    public List<EventResponseDto> getEventsByInvitedUser(int ownerId, int userInvitedId) {
        return null;
    }

    @Override
    public List<UserDto> addEventNewGuest(long eventId, UserDto user) {
        return null;
    }
}
