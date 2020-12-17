package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.EventModel;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.MainMenu;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.messaging.ReplyMessagesService;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants.*;

/**
 * @author Greg Adler
 */
@Component
public class StepBackInputMessageHandler implements InputMessageHandler {
    private MainMenu mainMenu;
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;


    public StepBackInputMessageHandler(UserEventDataCache userEventDataCache,
                                       MainMenu mainMenu, 
                                       ReplyMessagesService replyMessagesService) {
        this.mainMenu=mainMenu;
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService=replyMessagesService;
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
            return processBackToTitleRequest(userId, chatId, FILLING_EVENT_FORM, ASK_DESCRIPTION, "reply.askTitle");
        }
        if (usersCurrentBotStateStep==ASK_MONTH){
            return processBackToDescriptionRequest(userId, chatId, FILLING_EVENT_FORM, ASK_YEAR, "reply.askDescription");
        }
        if (usersCurrentBotStateStep==ASK_DAY){
            return processBackToYearRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep==ASK_HOUR){
            return processBackToMonthRequest(userId, chatId);
        }     
        if (usersCurrentBotStateStep==ASK_TEMPLATE){
            return processBackToDayRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep== EVENT_FORM_VALIDATION){
            return processBackToHourRequest(userId, chatId);
        }
        if (usersCurrentBotStateStep== EVENT_FORM_CONFIRMATION){
            return processBackToTemplateRequest(userId, chatId);
        }

        return mainMenu.getMainMenuMessage(chatId,answer);
    }

    private BotApiMethod<?> processBackToTemplateRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,EVENT_FILLED);
        userEventDataCache.setUsersCurrentBotStateStep(userId, EVENT_FORM_VALIDATION);
        return replyMessagesService.getReplyMessage(chatId,"reply.askTemplate");
    }

    private BotApiMethod<?> processBackToHourRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_TEMPLATE);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askHour");
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(HOUR_CALENDAR);
        if (calendarKeyboard==null){
            calendarKeyboard = getReplyMarkup(1, 24);
            userEventDataCache.setCalendarKeyboard(HOUR_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processBackToDayRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_HOUR);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askDay");
        EventModel userEventData = userEventDataCache.getCurrentEventData(userId);
        YearMonth yearMonthObject = YearMonth.of(userEventData.getYear(), userEventData.getMonth());
        int daysInMonth = yearMonthObject.lengthOfMonth();
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth + "_days");
        if (calendarKeyboard==null){
            calendarKeyboard = getReplyMarkup(1, 12);
            userEventDataCache.setCalendarKeyboard(daysInMonth + "_days",calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processBackToMonthRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_DAY);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMonth");
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
        if (calendarKeyboard==null){
            calendarKeyboard = getReplyMarkup(1, 12);
            userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processBackToYearRequest(int userId, long chatId) {
        userEventDataCache.setUsersCurrentBotState(userId,FILLING_EVENT_FORM);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_MONTH);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askYear");
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_CALENDAR);
        if (calendarKeyboard==null){
            int yearNow = LocalDate.now().getYear();
             calendarKeyboard = getReplyMarkup(yearNow, yearNow + 5);
             userEventDataCache.setCalendarKeyboard(YEAR_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processBackToDescriptionRequest(int userId, long chatId, BotState fillingEventForm, BotStateStep askYear, String s) {
        userEventDataCache.setUsersCurrentBotState(userId, fillingEventForm);
        userEventDataCache.setUsersCurrentBotStateStep(userId, askYear);
        return replyMessagesService.getReplyMessage(chatId, s);
    }

    private BotApiMethod<?> processBackToTitleRequest(int userId, long chatId, BotState fillingEventForm, BotStateStep askDescription, String s) {
        userEventDataCache.setUsersCurrentBotState(userId, fillingEventForm);
        userEventDataCache.setUsersCurrentBotStateStep(userId, askDescription);
        return replyMessagesService.getReplyMessage(chatId, s);
    }


    private ReplyKeyboard getReplyMarkup(int from, int to) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>buttons=new ArrayList<>();
        for (int i = from; i <= to; ) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            for (int j=from; j<=to && j<=6;j++,i++){
                keyboardButtonsRow.add(getButton(j+"",j+""));
            }
            buttons.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardButton getButton(String text,String callbackValue) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        return button.setCallbackData(callbackValue);
    }



    @Override
    public BotState getHandlerName() {
        return STEP_BACK;
    }
}
