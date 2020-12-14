package gregad.eventmanager.eventcreatorbot.bot.messaging;

import gregad.eventmanager.eventcreatorbot.bot.BotState;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.ImageResponseDto;
import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import gregad.eventmanager.eventcreatorbot.service.image_service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Greg Adler
 */
@Component
public class FillingEventFormInputMessageHandler implements InputMessageHandler {
    
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;
    private EventService eventService;
    private ImageService imageService;

    @Autowired
    public FillingEventFormInputMessageHandler(UserEventDataCache userEventDataCache, 
                                               ReplyMessagesService replyMessagesService, 
                                               EventService eventService, 
                                               ImageService imageService) {
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService = replyMessagesService;
        this.eventService = eventService;
        this.imageService = imageService;
    }
    

    @Override
    public BotApiMethod<?> handle(Message message) {
        if (userEventDataCache.getUsersCurrentBotState(message.getFrom().getId()).equals(BotState.FILLING_EVENT_FORM)) {
            userEventDataCache.setUsersCurrentBotState(message.getFrom().getId(), BotState.ASK_TIME);
        }
        return processUserInput(message);
    }

    private SendMessage processUserInput(Message inputMessage) {
        String answer=inputMessage.getText();
        int userId = inputMessage.getFrom().getId();
        long chatId = inputMessage.getChatId();

        BotState userCurrentBotState = userEventDataCache.getUsersCurrentBotState(userId);
        EventDto userEventData = userEventDataCache.getUserEventData(userId);
        
        if (userCurrentBotState==BotState.ASK_TITLE){
            userEventDataCache.setUsersCurrentBotState(userId,BotState.ASK_DESCRIPTION);
            return replyMessagesService.getReplyMessage(chatId,"reply.askTitle");
        }
        if (userCurrentBotState==BotState.ASK_DESCRIPTION){
            if (answer==null || answer.isEmpty()){
                return replyMessagesService.getReplyMessage(chatId,"reply.askTitleRepeated");
            }
            userEventData.setTitle(answer);
            userEventDataCache.setUsersCurrentBotState(userId,BotState.ASK_DATE);
            return replyMessagesService.getReplyMessage(chatId,"reply.askDescription");
        }
        if (userCurrentBotState==BotState.ASK_DATE){
            userEventData.setDescription(answer==null?"":answer);
            userEventDataCache.setUsersCurrentBotState(userId,BotState.ASK_TIME);
            return replyMessagesService.getReplyMessage(chatId,"reply.askDate");
        }
        if (userCurrentBotState==BotState.ASK_TIME){
            if (answer==null){
                return replyMessagesService.getReplyMessage(chatId,"reply.askDateRepeated");
            }
            userEventData.setEventDate(LocalDate.parse(answer));
            userEventDataCache.setUsersCurrentBotState(userId,BotState.ASK_TEMPLATE);
            return replyMessagesService.getReplyMessage(chatId,"reply.askTime");
        }
        if (userCurrentBotState==BotState.ASK_TEMPLATE){
            userEventData.setEventTime(LocalTime.parse(answer));
            userEventDataCache.setUsersCurrentBotState(userId,BotState.EVENT_FILLED);
            return replyMessagesService.getReplyMessage(chatId,"reply.askTemplate");
        }
        if (userCurrentBotState==BotState.EVENT_FILLED){
            if (answer!=null || !answer.isEmpty()){
                ImageResponseDto image = imageService.createImage(Integer.parseInt(answer), userEventData);
                userEventData.setImageUrl(image.getSelf());
            }
            userEventData.setOwner(userEventDataCache.getUserData(userId));
            userEventData.setTelegramChannelRef(createTelegramChannel(userId));
            eventService.createEvent(userEventData);
            userEventDataCache.setUsersCurrentBotState(userId,BotState.SHOW_MAIN_MENU);
        }
        userEventDataCache.setUsersCurrentBotState(userId,BotState.ASK_TITLE);
        return replyMessagesService.getReplyMessage(chatId,"reply.askTitle");
    }

    private String createTelegramChannel(int userId) {
        //todo create channel add assistant and user and return reference
        return "";
    }

    @Override
    public BotState getHandlerName() {
        return BotState.FILLING_EVENT_FORM;
    }
}
