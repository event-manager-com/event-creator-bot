package gregad.eventmanager.eventcreatorbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;

/**
 * @author Greg Adler
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponseDto {
    private ImageIO image;
    private String self;
}
