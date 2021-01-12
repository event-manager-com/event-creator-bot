package gregad.eventmanager.eventcreatorbot.service.user_service;

import gregad.eventmanager.eventcreatorbot.dto.UserDto;

/**
 * @author Greg Adler
 */
public interface UserService {
    UserDto createUser(int id, String name);

    UserDto updateUser(int id, String name);

    UserDto deleteUser(int id);

    UserDto getUser(int id);
}
