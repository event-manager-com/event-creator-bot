package gregad.eventmanager.eventcreatorbot.service.image_service;

import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.ImageResponseDto;

/**
 * @author Greg Adler
 */
public interface ImageService {
    ImageResponseDto createImage(int imageId, EventDto eventDto);
}
