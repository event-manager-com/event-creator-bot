package gregad.eventmanager.eventcreatorbot.rest;

import gregad.eventmanager.eventcreatorbot.bot.Bot;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants;
import gregad.eventmanager.eventcreatorbot.service.image_service.ImageService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;

/**
 * @author Greg Adler
 */
@RestController
public class BotController {

    @Autowired
    private Bot bot;

    @RequestMapping(value = "/template/{templateId}", method = {RequestMethod.GET}, produces = MediaType.IMAGE_JPEG_VALUE)
    public void getImage(HttpServletResponse response, @PathVariable String templateId) throws IOException {
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        FileInputStream fileInputStream = new FileInputStream(new File(BotConstants.PATH_TO_TEMPLATES + templateId));
        StreamUtils.copy(fileInputStream, response.getOutputStream());
    }

    @RequestMapping(value = "**", method = RequestMethod.POST)
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);
    }
}
