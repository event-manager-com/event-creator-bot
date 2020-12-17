package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.EventModel;
import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.messaging.ReplyMessagesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants.*;

/**
 * @author Greg Adler
 */
@Component
public class FillingEventFormInputMessageHandler implements InputMessageHandler {
    
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;

    @Autowired
    public FillingEventFormInputMessageHandler(UserEventDataCache userEventDataCache, 
                                               ReplyMessagesService replyMessagesService) {
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService = replyMessagesService;
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

        if (usersCurrentBotStateStep==ASK_TEMPLATE){
            return processTemplateRequest(answer, userId, chatId, userEventData);
        }

        userEventDataCache.setUsersCurrentBotStateStep(userId, ASK_DESCRIPTION);
        return replyMessagesService.getReplyMessage(chatId,"reply.askTitle");
    }

    private BotApiMethod<?> processTemplateRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askHour");
            ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(HOUR_CALENDAR);
            if (calendarKeyboard==null){
                calendarKeyboard=getReplyMarkup(1,24);
                userEventDataCache.setCalendarKeyboard(HOUR_CALENDAR,calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }

        userEventData.setHour(Integer.parseInt(answer));
        userEventData.setMinute(0);
        userEventDataCache.setUsersCurrentBotState(userId,EVENT_FILLED);
        userEventDataCache.setUsersCurrentBotStateStep(userId, EVENT_FORM_VALIDATION);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askTemplate");
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>buttons=new ArrayList<>();
        InlineKeyboardButton button=new InlineKeyboardButton();
        button.setText("1");
        button.setCallbackData("1");
        button.setUrl("./dog1.jpg");
        buttons.add(Collections.singletonList(button));
        inlineKeyboardMarkup.setKeyboard(buttons);
        return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    private BotApiMethod<?> processDayRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMonth");
            ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
            if (calendarKeyboard==null){
                calendarKeyboard=getReplyMarkup(1,12);
                userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        userEventData.setMonth(Integer.parseInt(answer));
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_HOUR);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askDay");

        YearMonth yearMonthObject = YearMonth.of(userEventData.getYear(), userEventData.getMonth());
        int daysInMonth = yearMonthObject.lengthOfMonth();
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth + "_days");
        if (calendarKeyboard==null){
            calendarKeyboard=getReplyMarkup(1,daysInMonth);
            userEventDataCache.setCalendarKeyboard(daysInMonth + "_days",calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processMonthRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askYear");
            ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_CALENDAR);
            if (calendarKeyboard==null){
                int yearNow = LocalDate.now().getYear();
                calendarKeyboard=getReplyMarkup(yearNow,yearNow+5);
                userEventDataCache.setCalendarKeyboard(YEAR_CALENDAR,calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        userEventData.setYear(Integer.parseInt(answer));
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_DAY);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askMonth");
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
        if (calendarKeyboard==null){
            calendarKeyboard=getReplyMarkup(1,12);
            userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processYearRequest(String answer, int userId, long chatId, EventModel userEventData) {
        userEventData.setDescription(answer==null?"":answer);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_MONTH);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askYear");
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_CALENDAR);
        if (calendarKeyboard==null){
            int yearNow = LocalDate.now().getYear();
            calendarKeyboard=getReplyMarkup(yearNow,yearNow+5);
            userEventDataCache.setCalendarKeyboard(YEAR_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private BotApiMethod<?> processDescriptionRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (answer==null || answer.isEmpty()){
            return replyMessagesService.getReplyMessage(chatId,"reply.askTitleRepeated");
        }
        userEventData.setTitle(answer);
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_YEAR);
        return replyMessagesService.getReplyMessage(chatId,"reply.askDescription");
    }

    private BotApiMethod<?> processHourRequest(String answer, int userId, long chatId, EventModel userEventData) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askDay");
            YearMonth yearMonthObject = YearMonth.of(userEventData.getYear(), userEventData.getMonth());
            int daysInMonth = yearMonthObject.lengthOfMonth();
            ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth + "_days");
            if (calendarKeyboard==null){
                calendarKeyboard=getReplyMarkup(1,daysInMonth);
                userEventDataCache.setCalendarKeyboard(daysInMonth + "_days",calendarKeyboard);
            }
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        userEventData.setDay(Integer.parseInt(answer));
        userEventDataCache.setUsersCurrentBotStateStep(userId,ASK_TEMPLATE);
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askHour");
        ReplyKeyboard calendarKeyboard = userEventDataCache.getCalendarKeyboard(HOUR_CALENDAR);
        if (calendarKeyboard==null){
            calendarKeyboard=getReplyMarkup(1,24);
            userEventDataCache.setCalendarKeyboard(HOUR_CALENDAR,calendarKeyboard);
        }
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    private ReplyKeyboard getReplyMarkup(int from, int to) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>buttons=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            keyboardButtonsRow.add(getButton(i+"",i+""));
            if (i%6==0){
                buttons.add(keyboardButtonsRow);
                keyboardButtonsRow=new ArrayList<>();
            }
        }
        buttons.add(keyboardButtonsRow);
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
        return FILLING_EVENT_FORM;
    }
}
