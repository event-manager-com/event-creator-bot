package gregad.eventmanager.eventcreatorbot.service.event_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.service.token_service.TokenHolderService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.api.ApiConstants.*;

/**
 * @author Greg Adler
 */
@Component
public class EventServiceImpl implements EventService {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private TokenHolderService tokenHolderService;

    @Value("${event.service.url}")
    private String eventServiceUrl;

    public EventServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, TokenHolderService tokenHolderService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenHolderService = tokenHolderService;
    }

    @SneakyThrows
    @Override
    public EventResponseDto createEvent(EventDto event) {
        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String eventJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        return restTemplate.exchange(eventServiceUrl, HttpMethod.POST,
                new HttpEntity<>(eventJson, headers), EventResponseDto.class).getBody();
    }

    @SneakyThrows
    @Override
    public EventResponseDto updateEvent(int ownerId, EventDto event) {
        HttpHeaders headers = getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String eventJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        return restTemplate.exchange(eventServiceUrl + "/" + ownerId,
                HttpMethod.PATCH, new HttpEntity<>(eventJson, headers), EventResponseDto.class).getBody();
    }

    @Override
    public EventResponseDto deleteEvent(int ownerId, long eventId) {
        HttpHeaders headers = getHeaders();
        return restTemplate.exchange(eventServiceUrl + "?ownerId=" + ownerId + "&eventId=" + eventId,
                HttpMethod.DELETE, new HttpEntity<>(headers), EventResponseDto.class).getBody();
    }

    @Override
    public EventResponseDto getEventById(int ownerId, long eventId) {
        HttpHeaders headers = getHeaders();
        return restTemplate.exchange(eventServiceUrl + "?ownerId=" + ownerId + "&eventId=" + eventId,
                HttpMethod.GET, new HttpEntity<>(headers), EventResponseDto.class).getBody();
    }

    @Override
    public List<EventResponseDto> getEventByTitle(int ownerId, String title) {
        HttpHeaders headers = getHeaders();
        return Arrays.asList(restTemplate.exchange(eventServiceUrl + SEARCH + BY_TITLE + "?ownerId=" + ownerId + "&title=" + title,
                HttpMethod.GET, new HttpEntity<>(headers), EventResponseDto[].class).getBody());
    }

    @Override
    public List<EventResponseDto> getFutureEvents(int ownerId) {
        HttpHeaders headers = getHeaders();
        return Arrays.asList(restTemplate.exchange(eventServiceUrl + SEARCH + "/" + ownerId,
                HttpMethod.GET, new HttpEntity<>(headers), EventResponseDto[].class).getBody());
    }

    @Override
    public List<EventResponseDto> getEventsByDate(int ownerId, LocalDate from, LocalDate to) {
        HttpHeaders headers = getHeaders();
        return Arrays.asList(restTemplate.exchange(eventServiceUrl + SEARCH + BY_DATES + "?ownerId=" + ownerId + "&from=" + from + "&to=" + to,
                HttpMethod.GET, new HttpEntity<>(headers), EventResponseDto[].class).getBody());
    }

    @Override
    public List<EventResponseDto> getEventsByInvitedUser(int ownerId, String userInvited) {
        HttpHeaders headers = getHeaders();
        return Arrays.asList(restTemplate.exchange(eventServiceUrl + SEARCH + BY_GUEST + "?ownerId=" + ownerId + "&guest=" + userInvited,
                HttpMethod.GET, new HttpEntity<>(headers), EventResponseDto[].class).getBody());
    }


    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER, tokenHolderService.getToken());
        return httpHeaders;
    }


}