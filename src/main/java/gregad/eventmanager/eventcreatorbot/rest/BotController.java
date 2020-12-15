package gregad.eventmanager.eventcreatorbot.rest;

import gregad.eventmanager.eventcreatorbot.bot.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Greg Adler
 */
@RestController
public class BotController {

    @Autowired
    private Bot bot;

    @RequestMapping(value = "**", method = RequestMethod.POST)
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);
    }
}
