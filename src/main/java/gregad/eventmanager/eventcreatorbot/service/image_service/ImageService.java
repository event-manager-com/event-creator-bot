package gregad.eventmanager.eventcreatorbot.service.image_service;

import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.ImageResponseDto;

import javax.imageio.stream.ImageOutputStream;

/**
 * @author Greg Adler
 */
public interface ImageService {
    ImageResponseDto createImage(String imageId, EventDto eventDto);

    byte[] getTemplateById(String templateId);
}
