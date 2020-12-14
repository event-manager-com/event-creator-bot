package gregad.eventmanager.eventcreatorbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Greg Adler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private UserDto user;
    private String text;
}
