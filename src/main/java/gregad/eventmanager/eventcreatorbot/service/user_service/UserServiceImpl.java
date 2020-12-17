package gregad.eventmanager.eventcreatorbot.service.user_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import gregad.eventmanager.eventcreatorbot.service.token_service.TokenHolderService;
import gregad.eventmanager.eventcreatorbot.service.user_service.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import static gregad.eventmanager.eventcreatorbot.api.ApiConstants.*;

/**
 * @author Greg Adler
 */
@Component
public class UserServiceImpl implements UserService {
    
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private TokenHolderService tokenHolderService;

    @Autowired
    public UserServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, TokenHolderService tokenHolderService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenHolderService = tokenHolderService;
    }

    @Value("${user.service.url}")
    private String userServiceUrl;
    
    @Override
    public UserDto createUser(int id,String name) {
        HttpHeaders httpHeaders=getHeaders();
        ResponseEntity<UserDto> response =
                restTemplate.exchange(userServiceUrl+"?telegramId="+id+"&name="+name,
                        HttpMethod.POST,
                        new HttpEntity<>(httpHeaders),
                        UserDto.class);
        return response.getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER,tokenHolderService.getToken());
        return httpHeaders;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SneakyThrows
    @Override
    public UserDto updateUser(int id, String name) {
        HttpHeaders httpHeaders=getHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String userJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new UserDto(id, name));
        ResponseEntity<UserDto> response =
                restTemplate.exchange(userServiceUrl+"/"+id,
                        HttpMethod.PATCH,
                        new HttpEntity<>(userJson,httpHeaders),
                        UserDto.class);
        return response.getBody();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public UserDto deleteUser(int id) {
        HttpHeaders httpHeaders=getHeaders();
        ResponseEntity<UserDto> response =
                restTemplate.exchange(userServiceUrl+"/"+id,
                        HttpMethod.DELETE,
                        new HttpEntity<>(httpHeaders),
                        UserDto.class);
        return response.getBody();
    }

    @Override
    public UserDto getUser(int id) {
        HttpHeaders httpHeaders=getHeaders();
        ResponseEntity<UserDto> response =
                restTemplate.exchange(userServiceUrl+"/"+id,
                        HttpMethod.GET,
                        new HttpEntity<>(httpHeaders),
                        UserDto.class);
        return response.getBody();
    }
}
