package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model.EventModel;
import gregad.eventmanager.eventcreatorbot.bot.MainMenu;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.KeyboardMarkupService;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.ReplyMessagesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.YearMonth;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep.*;

/**
 * @author Greg Adler
 */
@Component
public class StepBackInputMessageHandler implements InputMessageHandler {
    private MainMenu mainMenu;
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;
    private KeyboardMarkupService keyboardMarkupService;
    @Value("${bot.webHookPath}")
    private String myUrl;


    public StepBackInputMessageHandler(UserEventDataCache userEventDataCache,
                                       MainMenu mainMenu, 
                                       ReplyMessagesService replyMessagesService,
                                       KeyboardMarkupService keyboardMarkupService) {
        this.mainMenu=mainMenu;
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService=replyMessagesService;
        this.keyboardMarkupService=keyboardMarkupService;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
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
        
        if (usersCurrentBotStateStep==NO_STATE_STEP ||
                usersCurrentBotStateStep==ASK_TITLE ||
                usersCurrentBotStateStep==ASK_DESCRIPTION){
            userEventDataCache.setUsersCurrentBotState(userId,BotState.SHOW_MAIN_MENU);
            return mainMenu.getMainMenuMessage(chatId," ");
        }
        if (usersCurrentBotStateStep==ASK_YEAR){
            return processBackToTitleRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep==ASK_MONTH){
            return processBackToDescriptionRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep==ASK_DAY){
            return processBackToYearRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep==ASK_HOUR){
            return processBackToMonthRequest(userId, chatId);
        }     
        if (usersCurrentBotStateStep==ASK_EVENT_TYPE){
            return processBackToDayRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep==ASK_TEMPLATE){
            return processBackToHourRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep== EVENT_FORM_VALIDATION){
            return processBackToTypeRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep== EVENT_FORM_CONFIRMATION){
            return processBackToTemplateRequest(userId, chatId);
        }
        
        if (usersCurrentBotStateStep==MY_FUTURE_EVENTS ||
            usersCurrentBotStateStep==MY_EVENTS_BY_GUEST ||
            usersCurrentBotStateStep==MY_EVENTS_BY_TITLE||
            usersCurrentBotStateStep==SHOW_FILTERED_EVENTS_BY_DATES_WAIT|| 
            usersCurrentBotStateStep==MY_EVENTS_BY_DATE_FROM_YEAR||
            usersCurrentBotStateStep==MY_EVENTS_BY_DATE_FROM_MONTH){
            return processBackToFiltersOptions(userId,chatId);
        }

        return mainMenu.getMainMenuMessage(chatId,answer);
    }
/////////////////////////////////////////////    CHOOSE_EVENT_FILTER
    private BotApiMethod<?> processBackToFiltersOptions(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,GET_EVENTS);
        userEventDataCache.setUsersCurrentBotStateStep(userId,CHOOSE_EVENT_FILTER);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMyEventFilterChoose");
        InlineKeyboardMarkup inlineKeyboardMarkup=keyboardMarkupService.getFilterOptionsKeyboardMarkup();
        return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    //////////////////////////////////////////    TEMPLATE 
    
    private BotApiMethod<?> processBackToTemplateRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,EVENT_FILLED);
        userEventDataCache.setUsersCurrentBotStateStep(userId, EVENT_FORM_VALIDATION);
        EventModel currentEventData = userEventDataCache.getCurrentEventData(userId);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askTemplate");
        String eventType = currentEventData.getEventType();
        InlineKeyboardMarkup imageTemplateKeyboard = userEventDataCache.getImageTemplateKeyboard(eventType);
        if (isEmptyKeyboard(imageTemplateKeyboard)){
            imageTemplateKeyboard=keyboardMarkupService.getTemplatesInlineKeyboardMarkup(myUrl,eventType);
            userEventDataCache.setImageTemplateKeyboard(eventType,imageTemplateKeyboard);
        }
        return replyMessage.setReplyMarkup(imageTemplateKeyboard);
    }

//////////////////////////////////////////    EVENT_TYPE    

    private BotApiMethod<?> processBackToTypeRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId, ASK_TEMPLATE);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askEventType");
        InlineKeyboardMarkup eventTypesInlineKeyboardMarkup = keyboardMarkupService.getEventTypesInlineKeyboardMarkup();
        return replyMessage.setReplyMarkup(eventTypesInlineKeyboardMarkup);
    }

//////////////////////////////////////////    HOUR 
    
    private BotApiMethod<?> processBackToHourRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_EVENT_TYPE);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askHour");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(HOUR_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard = keyboardMarkupService.getInlineKeyboardMarkupInRange(1, 24);
            userEventDataCache.setCalendarKeyboard(HOUR_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    //////////////////////////////////////////    DAY    
    
    private BotApiMethod<?> processBackToDayRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_HOUR);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askDay");
        EventModel userEventData = userEventDataCache.getCurrentEventData(userId);
        YearMonth yearMonthObject = YearMonth.of(userEventData.getYear(), userEventData.getMonth());
        int daysInMonth = yearMonthObject.lengthOfMonth();
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth + DAY_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard = keyboardMarkupService.getInlineKeyboardMarkupInRange(1, daysInMonth);
            userEventDataCache.setCalendarKeyboard(daysInMonth + DAY_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    //////////////////////////////////////////    MONTH
    
    private BotApiMethod<?> processBackToMonthRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_DAY);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMonth");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard = keyboardMarkupService.getInlineKeyboardMarkupInRange(1, 12);
            userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    //////////////////////////////////////////    YEAR
    
    private BotApiMethod<?> processBackToYearRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_MONTH);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askYear");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_FUTURE_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            int yearNow = LocalDate.now().getYear();
             calendarKeyboard = keyboardMarkupService.getInlineKeyboardMarkupInRange(yearNow, yearNow + 5);
             userEventDataCache.setCalendarKeyboard(YEAR_FUTURE_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    //////////////////////////////////////////    DESCRIPTION
    
    private BotApiMethod<?> processBackToDescriptionRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId, BotState.FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId, BotStateStep.ASK_YEAR);
        return replyMessagesService.getReplyMessage(chatId, "reply.askDescription");
    }

    //////////////////////////////////////////    TITLE

    private BotApiMethod<?> processBackToTitleRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId, BotState.FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId, BotStateStep.ASK_DESCRIPTION);
        return replyMessagesService.getReplyMessage(chatId, "reply.askTitle");
    }
    
    private boolean isEmptyKeyboard(InlineKeyboardMarkup calendarKeyboard) {
        return calendarKeyboard.getKeyboard()==null|| calendarKeyboard.getKeyboard().isEmpty();
    }



    @Override
    public BotState getHandlerName() {
        return STEP_BACK;
    }
}
