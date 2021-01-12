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

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

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

    @SneakyThrows
    @Override
    public byte[] getTemplateById(String templateId) {
        File file = new File("./" + templateId);
        byte[] bytes = Files.readAllBytes(file.toPath());
        return bytes;
//        try( InputStream is = this.getClass().getResourceAsStream("./" + templateId);) {
//            BufferedImage img = ImageIO.read(is);
//            ByteArrayOutputStream bao = new ByteArrayOutputStream();
//            ImageIO.write(img, "jpg", bao);
//            return bao.toByteArray();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
