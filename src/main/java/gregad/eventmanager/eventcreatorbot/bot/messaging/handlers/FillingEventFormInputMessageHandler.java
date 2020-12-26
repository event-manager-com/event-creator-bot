package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model.EventModel;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.KeyboardMarkupService;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.ReplyMessagesService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.YearMonth;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.EVENT_FILLED;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.FILLING_EVENT_FORM;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep.*;

/**
 * @author Greg Adler
 */
@Component
public class FillingEventFormInputMessageHandler implements InputMessageHandler {
    
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;
    private KeyboardMarkupService keyboardMarkupService;
    @Value("${bot.webHookPath}")
    private String myUrl;

    @Autowired
    public FillingEventFormInputMessageHandler(UserEventDataCache userEventDataCache,
                                               ReplyMessagesService replyMessagesService,
                                               KeyboardMarkupService keyboardMarkupService) {
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService = replyMessagesService;
        this.keyboardMarkupService=keyboardMarkupService;
    }
    

    @Override
    public BotApiMethod<?> handle(Update update) {
        
        User from;
        if (update.hasCallbackQuery()){
            from=update.getCallbackQuery().getFrom();
        }else {
            from=update.getMessage().getFrom();
        }
        if (userEventDataCache.getUsersCurrentBotStateStep(from.getId())==NO_STATE_STEP) {
            userEventDataCache.setUsersCurrentBotStateStep(from.getId(), ASK_TITLE);
        }
        return processUserInput(update);
    }

    private BotApiMethod<?> processUserInput(Update update) {
        String answer;
        int userId;
        long chatId;
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            answer=callbackQuery.getData();
            userId=callbackQuery.getFrom().getId();
            chatId=callbackQuery.getMessage().getChatId();
        }else {
            Message message = update.getMessage();
            answer=message.getText();
            userId = message.getFrom().getId();
            chatId = message.getChatId();
        }


        BotStateStep usersCurrentBotStateStep = userEventDataCache.getUsersCurrentBotStateStep(userId);
        EventModel userEventData = userEventDataCache.getCurrentEventData(userId);
        if (usersCurrentBotStateStep==ASK_TITLE){
            userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_DESCRIPTION);
            return replyMessagesService.getReplyMessage(chatId,"reply.askTitle");
        }
        if (usersCurrentBotStateStep==ASK_DESCRIPTION){
            return processDescriptionRequest(answer, userId, chatId, userEventData);
        }
        if (usersCurrentBotStateStep==ASK_YEAR){
            return processYearRequest(answer, userId, chatId, userEventData);
        }
        if (usersCurrentBotStateStep==ASK_MONTH){
            return processMonthRequest(answer, userId, chatId, userEventData);
        }
        if (usersCurrentBotStateStep==ASK_DAY){
            return processDayRequest(answer, userId, chatId, userEventData);
        }
        if (usersCurrentBotStateStep==ASK_HOUR){
            return processHourRequest(answer, userId, chatId, userEventData);
        }
        if (usersCurrentBotStateStep==ASK_EVENT_TYPE){
            return processEventTypeRequest(answer, userId, chatId, userEventData);
        }
        if (usersCurrentBotStateStep==ASK_TEMPLATE){
            return processTemplateRequest(answer, userId, chatId, userEventData);
        }

        userEventDataCache.setUsersCurrentBotStateStep(userId, ASK_DESCRIPTION);
        return replyMessagesService.getReplyMessage(chatId,"reply.askTitle");
    }
//////////////////////////////////////////    EVENT_TYPE
    
    private BotApiMethod<?> processEventTypeRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askHour");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(HOUR_CALENDAR);
            if (isEmptyKeyboard(calendarKeyboard)){
                calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(1,24);
                userEventDataCache.setCalendarKeyboard(HOUR_CALENDAR,calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }

        int hour = Integer.parseInt(answer);
        userEventData.setHour(hour>24?0:hour);
        userEventData.setMinute(0);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_TEMPLATE);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askEventType");
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardMarkupService.getEventTypesInlineKeyboardMarkup();
        return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
    }
    
//////////////////////////////////////////    TEMPLATE

    @SneakyThrows
    private BotApiMethod<?> processTemplateRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (isIllegalAnswer(answer)){
            answer=OTHER;
        }
        userEventData.setEventType(answer);
        userEventDataCache.setUsersCurrentBotState(userId,EVENT_FILLED);
        userEventDataCache.setUsersCurrentBotStateStep(userId, EVENT_FORM_VALIDATION);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askTemplate");
        InlineKeyboardMarkup imageTemplateKeyboard = userEventDataCache.getImageTemplateKeyboard(answer);
        if (isEmptyKeyboard(imageTemplateKeyboard)){
            imageTemplateKeyboard=keyboardMarkupService.getTemplatesInlineKeyboardMarkup(myUrl,answer);
            userEventDataCache.setImageTemplateKeyboard(answer,imageTemplateKeyboard);
        }
        return replyMessage.setReplyMarkup(imageTemplateKeyboard);
    }

    private boolean isIllegalAnswer(String answer) {
       if( answer==null || answer.isEmpty() ){
           return true;
       }
       return !answer.equals(BIRTHDAY) &&
               !answer.equals(HOLIDAY) && 
               !answer.equals(FRIENDS_MEETING) &&
               !answer.equals(OFFICIAL_MEETING) &&
               !answer.equals(MARY_CHRISTMAS);
    }

    private boolean isEmptyKeyboard(InlineKeyboardMarkup calendarKeyboard) {
        return calendarKeyboard.getKeyboard()==null|| calendarKeyboard.getKeyboard().isEmpty();
    }

    //////////////////////////////////////////    DAY

    private BotApiMethod<?> processDayRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMonth");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
            if (isEmptyKeyboard(calendarKeyboard)){
                calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(1,12);
                userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        userEventData.setMonth(Integer.parseInt(answer));
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_HOUR);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askDay");

        YearMonth yearMonthObject = YearMonth.of(userEventData.getYear(), userEventData.getMonth());
        int daysInMonth = yearMonthObject.lengthOfMonth();
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth + DAY_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(1,daysInMonth);
            userEventDataCache.setCalendarKeyboard(daysInMonth + DAY_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

 //////////////////////////////////////////    MONTH

    private BotApiMethod<?> processMonthRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askYear");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_FUTURE_CALENDAR);
            if (isEmptyKeyboard(calendarKeyboard)){
                int yearNow = LocalDate.now().getYear();
                calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(yearNow,yearNow+5);
                userEventDataCache.setCalendarKeyboard(YEAR_FUTURE_CALENDAR,calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        userEventData.setYear(Integer.parseInt(answer));
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_DAY);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMonth");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(1,12);
            userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

//////////////////////////////////////////    YEAR

    private BotApiMethod<?> processYearRequest(String answer, int userId, long chatId, EventModel userEventData) {
        userEventData.setDescription(answer==null?"":answer);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_MONTH);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askYear");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_FUTURE_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            int yearNow = LocalDate.now().getYear();
            calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(yearNow,yearNow+5);
            userEventDataCache.setCalendarKeyboard(YEAR_FUTURE_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

//////////////////////////////////////////    DESCRIPTION

    private BotApiMethod<?> processDescriptionRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (answer==null || answer.isEmpty()){
            return replyMessagesService.getReplyMessage(chatId,"reply.askTitleRepeated");
        }
        userEventData.setTitle(answer);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_YEAR);
        return replyMessagesService.getReplyMessage(chatId,"reply.askDescription");
    }

//////////////////////////////////////////    HOUR

    private BotApiMethod<?> processHourRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askDay");
            YearMonth yearMonthObject = YearMonth.of(userEventData.getYear(), userEventData.getMonth());
            int daysInMonth = yearMonthObject.lengthOfMonth();
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth + "_days");
            if (isEmptyKeyboard(calendarKeyboard)){
                calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(1,daysInMonth);
                userEventDataCache.setCalendarKeyboard(daysInMonth + "_days",calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        userEventData.setDay(Integer.parseInt(answer));
        try {
            LocalDate.of(userEventData.getYear(),userEventData.getMonth(),userEventData.getDay());
        } catch (Exception e) {
            int year = LocalDate.now().getYear();
            SendMessage replyMessage =
                    replyMessagesService.getReplyMessage(chatId, "reply.askYear", e.getMessage() + "\n Try again");
            InlineKeyboardMarkup inlineKeyboardMarkupInRange =
                    keyboardMarkupService.getInlineKeyboardMarkupInRange(year, year + 5);
            return replyMessage.setReplyMarkup(inlineKeyboardMarkupInRange);
        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_EVENT_TYPE);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askHour");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(HOUR_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard= keyboardMarkupService.getInlineKeyboardMarkupInRange(1,24);
            userEventDataCache.setCalendarKeyboard(HOUR_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    @Override
    public BotState getHandlerName() {
        return FILLING_EVENT_FORM;
    }
}
