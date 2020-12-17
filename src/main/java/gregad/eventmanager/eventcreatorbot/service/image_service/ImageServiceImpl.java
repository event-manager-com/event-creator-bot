package gregad.eventmanager.eventcreatorbot.service.image_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.ImageResponseDto;
import gregad.eventmanager.eventcreatorbot.service.token_service.TokenHolderService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author Greg Adler
 */
@Component
public class ImageServiceImpl implements ImageService {
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private TokenHolderService tokenHolderService;
    
    @Value("${image.service.url}")
    private String imageServiceUrl;

    @Autowired
    public ImageServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, TokenHolderService tokenHolderService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenHolderService = tokenHolderService;
    }

    @SneakyThrows
    @Override
    public ImageResponseDto createImage(String imageId, EventDto eventDto) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String eventJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventDto);
        ResponseEntity<ImageResponseDto> response = 
                restTemplate.exchange(imageServiceUrl + "/" + imageId,
                        HttpMethod.POST,
                        new HttpEntity<>(eventJson, httpHeaders), 
                        ImageResponseDto.class);
        
        return response.getBody();
    }
}
