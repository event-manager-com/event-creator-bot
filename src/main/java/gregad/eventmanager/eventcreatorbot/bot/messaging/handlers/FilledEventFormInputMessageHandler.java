package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model.EventModel;
import gregad.eventmanager.eventcreatorbot.bot.MainMenu;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.ReplyMessagesService;
import gregad.eventmanager.eventcreatorbot.dto.EventDto;
import gregad.eventmanager.eventcreatorbot.dto.ImageResponseDto;
import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import gregad.eventmanager.eventcreatorbot.service.image_service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;

/**
 * @author Greg Adler
 */
@Component
public class FilledEventFormInputMessageHandler implements InputMessageHandler {

    private static final String SUBMIT_BUTTON_VALUE = "submit_event";
    private static final String STEP_BACK_BUTTON_VALUE = "step_back";
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;
    private MainMenu mainMenu;
    private EventService eventService;
    private ImageService imageService;

    @Autowired
    public FilledEventFormInputMessageHandler(UserEventDataCache userEventDataCache,
                                              ReplyMessagesService replyMessagesService,
                                              MainMenu mainMenu,
                                              EventService eventService,
                                              ImageService imageService) {
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService = replyMessagesService;
        this.mainMenu = mainMenu;
        this.eventService = eventService;
        this.imageService = imageService;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        String answer;
        int userId;
        long chatId;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            answer = callbackQuery.getData();
            userId = callbackQuery.getFrom().getId();
            chatId = callbackQuery.getMessage().getChatId();
        } else {
            Message message = update.getMessage();
            answer = message.getText();
            userId = message.getFrom().getId();
            chatId = message.getChatId();
        }


        BotStateStep usersCurrentBotStateStep = userEventDataCache.getUsersCurrentBotStateStep(userId);
        EventModel userEventData = userEventDataCache.getCurrentEventData(userId);

        if (usersCurrentBotStateStep == EVENT_FORM_VALIDATION) {
            return processEventFormValidation(answer, userId, chatId, userEventData);
        }

        if (usersCurrentBotStateStep == EVENT_FORM_CONFIRMATION) {
            if (answer.equals(SUBMIT_BUTTON_VALUE)) {
                EventDto event = convertToEventDto(userEventData);
                if (userEventData.getImageUrl() != null || !userEventData.getImageUrl().isEmpty()) {
                    ImageResponseDto image = imageService.createImage(userEventData.getImageUrl(), event);
                    userEventData.setImageUrl(image.getSelf());
                } else {
                    userEventData.setImageUrl(createTextInvitation(userEventData));
                }
                eventService.createEvent(event);
                userEventDataCache.setUsersCurrentBotStateStep(userId, NO_STATE_STEP);
                userEventDataCache.setUsersCurrentBotState(userId, BotState.SHOW_MAIN_MENU);
                userEventDataCache.saveCurrentEventData(userId, userEventData);
            } else {
                userEventDataCache.setUsersCurrentBotStateStep(userId, EVENT_FORM_VALIDATION);
                return replyMessagesService.getReplyMessage(chatId, "reply.askTemplate");
            }
        }
        return mainMenu.getMainMenuMessage(chatId, answer);
    }

    private BotApiMethod<?> processEventFormValidation(String answer, int userId, long chatId, EventModel userEventData) {
        userEventData.setOwner(userEventDataCache.getUserData(userId));
        userEventData.setTelegramChannelRef(createTelegramChannel(userId));
        userEventData.setImageUrl(answer);
        userEventDataCache.setUsersCurrentBotStateStep(userId, EVENT_FORM_CONFIRMATION);
        String message = buildMessage(userEventData);
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.setReplyMarkup(getReplyMarkup());
        return sendMessage;
    }

    private EventDto convertToEventDto(EventModel userEventData) {
        EventDto eventDto = new EventDto();
        eventDto.setOwner(userEventData.getOwner());
        eventDto.setTitle(userEventData.getTitle());
        eventDto.setDescription(userEventData.getDescription());
        eventDto.setImageUrl(userEventData.getImageUrl());
        eventDto.setTelegramChannelRef(userEventData.getTelegramChannelRef());
        LocalDate date = LocalDate.of(userEventData.getYear(), userEventData.getMonth(), userEventData.getDay());
        eventDto.setEventDate(date);
        LocalTime time = LocalTime.of(userEventData.getHour(), userEventData.getMinute());
        eventDto.setEventTime(time);
        return eventDto;
    }

    private String createTextInvitation(EventModel userEventData) {
        StringBuilder sb = new StringBuilder();
        sb.append(userEventData.getOwner().getName()).append(" invites you to an event \n");
        sb.append(userEventData.getTitle()).append("\n");

        sb.append("which will take place on the ")
                .append(userEventData.getYear())
                .append("/")
                .append(userEventData.getMonth())
                .append("/")
                .append(userEventData.getDay());
        sb.append("at ")
                .append(userEventData.getHour())
                .append(":")
                .append(userEventData.getMinute())
                .append(" o'clock\n");
        if (userEventData.getDescription() != null || !userEventData.getDescription().isEmpty()) {
            sb.append("Description: \n").append(userEventData.getDescription());
        }
        sb.append("to receive and discuss details, follow the link\n");
        sb.append(userEventData.getTelegramChannelRef());
        return sb.toString();
    }

    private String buildMessage(EventModel userEventData) {
        String date = userEventData.getYear() + "/" + userEventData.getMonth() + "/" + userEventData.getDay();
        String time = userEventData.getHour() + ":" + userEventData.getMinute();
        return "User name: " + userEventData.getOwner().getName() + "\n" +
                "Title: " + userEventData.getTitle() + "\n" +
                "Description: " + userEventData.getDescription() + "\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Image template: " + userEventData.getImageUrl() + "\n\n" +
                "Press <Submit> to generate invitation,\n or <Step back> to edit";
    }

    private ReplyKeyboard getReplyMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton buttonSubmit = getButton("Submit", SUBMIT_BUTTON_VALUE);
        InlineKeyboardButton buttonBack = getButton("Step back", STEP_BACK_BUTTON_VALUE);
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(buttonSubmit);
        keyboardButtonsRow1.add(buttonBack);
        return inlineKeyboardMarkup.setKeyboard(Collections.singletonList(keyboardButtonsRow1));
    }

    private InlineKeyboardButton getButton(String text, String callbackValue) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        return button.setCallbackData(callbackValue);
    }

    private String createTelegramChannel(int userId) {
        //todo create channel add assistant and user and return reference
        return "";
    }

    @Override
    public BotState getHandlerName() {
        return EVENT_FILLED;
    }
}
