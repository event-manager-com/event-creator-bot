package gregad.eventmanager.eventcreatorbot.service.token_service;

/**
 * @author Greg Adler
 */
public interface TokenHolderService {
    void refreshToken();
    String getToken();
}
