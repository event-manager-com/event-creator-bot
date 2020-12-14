package gregad.eventmanager.eventcreatorbot.service.token_service;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Greg Adler
 */
@Data
@AllArgsConstructor
public class NamePassword {
    private String name;
    private String password;
}
