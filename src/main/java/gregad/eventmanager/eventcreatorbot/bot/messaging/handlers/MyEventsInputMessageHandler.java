package gregad.eventmanager.eventcreatorbot.bot.messaging.handlers;

import gregad.eventmanager.eventcreatorbot.bot.cache.UserEventDataCache;
import gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model.EventFilterDates;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotState;
import gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.KeyboardMarkupService;
import gregad.eventmanager.eventcreatorbot.bot.messaging.utils.ReplyMessagesService;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import gregad.eventmanager.eventcreatorbot.service.event_service.EventService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotState.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotStateStep.*;
import static gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants.*;

/**
 * @author Greg Adler
 */
@Component
public class MyEventsInputMessageHandler implements InputMessageHandler {
    private UserEventDataCache userEventDataCache;
    private ReplyMessagesService replyMessagesService;
    private KeyboardMarkupService keyboardMarkupService;
    private EventService eventService;

    @Autowired
    public MyEventsInputMessageHandler(UserEventDataCache userEventDataCache,
                                       ReplyMessagesService replyMessagesService,
                                       KeyboardMarkupService keyboardMarkupService,
                                       EventService eventService) {
        this.userEventDataCache = userEventDataCache;
        this.replyMessagesService = replyMessagesService;
        this.keyboardMarkupService=keyboardMarkupService;
        this.eventService=eventService;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId;
        int userId;
        String answer;
        if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            chatId=callbackQuery.getMessage().getChatId();
            userId=callbackQuery.getFrom().getId();
            answer=callbackQuery.getData();
        }else {
            Message message = update.getMessage();
            chatId=message.getChatId();
            userId=message.getFrom().getId();
            answer=message.getText();
        }
        BotStateStep usersCurrentBotStateStep = userEventDataCache.getUsersCurrentBotStateStep(userId);

        if (usersCurrentBotStateStep==NO_STATE_STEP){
            return processNoStateStep(chatId,userId,"");
        }
        if (usersCurrentBotStateStep==CHOOSE_EVENT_FILTER){
            usersCurrentBotStateStep=
                    answer.equals(EVENTS_BY_TITLE)?MY_EVENTS_BY_TITLE:
                            answer.equals(EVENTS_BY_GUEST)?MY_EVENTS_BY_GUEST:
                                    answer.equals(EVENTS_BETWEEN_DATES)?MY_EVENTS_BY_DATE_FROM_YEAR:MY_FUTURE_EVENTS;
            userEventDataCache.setUsersCurrentBotStateStep(userId,usersCurrentBotStateStep);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_TITLE){
            return processEventByTitle(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_GUEST){
            return processEventByGuest(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_FUTURE_EVENTS){
            return processFutureEvents(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_DATE_FROM_YEAR){
            return processEventsFromYear(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_DATE_FROM_MONTH){
            return processEventsFromMonth(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_DATE_FROM_DAY){
            return processEventsFromDay(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_DATE_TO_YEAR){
            return processEventsToYear(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_DATE_TO_MONTH){
            return processEventsToMonth(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==MY_EVENTS_BY_DATE_TO_DAY){
            return processEventsToDay(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==SHOW_FILTERED_EVENTS_BY_DATES){
            return processShowEventsByDates(chatId,userId,answer);
        }
        if (usersCurrentBotStateStep==SHOW_FILTERED_EVENTS_BY_DATES_WAIT){
            return processShowEventsByDatesDisplayChosenEvent(chatId,userId,answer);
        }
         
        return processNoStateStep(chatId,userId,"");
    }
////////////////////////////////  SHOW_FILTERED_EVENTS_BY_DATES_WAIT
    private BotApiMethod<?> processShowEventsByDatesDisplayChosenEvent(long chatId, int userId, String answer) {
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        LocalDate fromDate =
                LocalDate.of(eventFilterDates.getYearFrom(), eventFilterDates.getMonthFrom(), eventFilterDates.getDayFrom());
        LocalDate toDate =
                LocalDate.of(eventFilterDates.getYearTo(), eventFilterDates.getMonthTo(), eventFilterDates.getDayTo());
        String cachedListKey=EVENTS_BETWEEN_DATES + fromDate + toDate;
        if (answer.equals(REFRESH_FILTER)){
            List<EventResponseDto> events = eventService.getEventsByDate(userId,fromDate,toDate);
            SendMessage replyMessage = events.isEmpty()?
                    replyMessagesService.getReplyMessage(chatId,"reply.askEventsNotFound",
                            "between dates: "+fromDate+" and "+toDate):
                    replyMessagesService.getReplyMessage(chatId,"reply.empty");
            InlineKeyboardMarkup inlineKeyboardMarkup=keyboardMarkupService.getEventsKeyboardMarkup(events,1);
            userEventDataCache.setUserEventsByFilter(userId,cachedListKey,events);
            return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        List<EventResponseDto> userEventsByFilter = userEventDataCache.getUserEventsByFilter(userId, cachedListKey);
        if (userEventsByFilter.isEmpty()){
            userEventsByFilter= eventService.getEventsByDate(userId,fromDate,toDate);
            userEventDataCache.setUserEventsByFilter(userId,cachedListKey,userEventsByFilter);
        }
        String message="reply.empty";
        if (StringUtils.isNumeric(answer)){
            int id = Integer.parseInt(answer);
            String eventString=userEventsByFilter.stream().filter(e->e.getId()==id).findFirst().orElse(null).toString();
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, message,eventString);
            InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 2);
            return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
        }
        SendMessage replyMessage =
                replyMessagesService.getReplyMessage(chatId,"reply.askEventsNotFound",
                        userEventsByFilter.isEmpty()?
                                "between dates: "+fromDate+" and "+toDate:"reply.empty");
        InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 1);
        return replyMessage.setReplyMarkup(eventsKeyboardMarkup);

    }

    ////////////////////////////   SHOW_FILTERED_EVENTS_BY_DATES
    private BotApiMethod<?> processShowEventsByDates(long chatId, int userId, String answer) {
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateDay");
            YearMonth yearMonthObject = YearMonth.of(eventFilterDates.getYearFrom(), eventFilterDates.getMonthFrom());
            int daysInMonth = yearMonthObject.lengthOfMonth();
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth+ DAY_CALENDAR);
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        int dayTo = Integer.parseInt(answer);
        eventFilterDates.setDayFrom(dayTo);
        userEventDataCache.setEventFilterDates(userId,eventFilterDates);

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.of(eventFilterDates.getYearFrom(), eventFilterDates.getMonthFrom(), eventFilterDates.getDayFrom());
            toDate = LocalDate.of(eventFilterDates.getYearTo(), eventFilterDates.getMonthTo(), dayTo);
        } catch (Exception e) {
            return processNoStateStep(chatId, userId,e.getMessage()+"\n Try again ");
        }
        if (fromDate.isAfter(toDate)) {
            return processNoStateStep(chatId, userId,"Date from can`t be after date till.\n Try again ");
        }
        List<EventResponseDto> userEventsByFilter =
                userEventDataCache.getUserEventsByFilter(userId, EVENTS_BETWEEN_DATES + fromDate + toDate);
        if (userEventsByFilter.isEmpty()){
            userEventsByFilter=eventService.getEventsByDate(userId,fromDate,toDate);
            userEventDataCache.setUserEventsByFilter(userId,EVENTS_BETWEEN_DATES+fromDate+toDate,userEventsByFilter);
        }
        
        SendMessage replyMessage =
                replyMessagesService.getReplyMessage(chatId,"reply.askEventsNotFound",
                        userEventsByFilter.isEmpty()?
                                "between dates: "+fromDate+" and "+toDate:"reply.empty");
        InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 1);
        userEventDataCache.setUsersCurrentBotStateStep(userId,SHOW_FILTERED_EVENTS_BY_DATES_WAIT);
        return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
    }

    ////////////////////////////    MY_EVENTS_BY_DATE_TO_DAY
    private BotApiMethod<?> processEventsToDay(long chatId, int userId, String answer) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateMonth");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        int month = Integer.parseInt(answer);
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        eventFilterDates.setMonthTo(month);
        userEventDataCache.setEventFilterDates(userId,eventFilterDates);
        
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateDay");
        YearMonth yearMonthObject = YearMonth.of(eventFilterDates.getYearFrom(), month);
        int daysInMonth = yearMonthObject.lengthOfMonth();
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth+ DAY_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard=keyboardMarkupService.getInlineKeyboardMarkupInRange(1,daysInMonth);
            userEventDataCache.setCalendarKeyboard(daysInMonth+ DAY_CALENDAR,calendarKeyboard);
        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,SHOW_FILTERED_EVENTS_BY_DATES);
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    ////////////////////////////    MY_EVENTS_BY_DATE_TO_MONTH
    private BotApiMethod<?> processEventsToMonth(long chatId, int userId, String answer) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateTo");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_PAST_AND_FUTURE_CALENDAR);
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        int year = Integer.parseInt(answer);
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        eventFilterDates.setYearTo(year);
        userEventDataCache.setEventFilterDates(userId,eventFilterDates);

        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateMonth");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard=keyboardMarkupService.getInlineKeyboardMarkupInRange(1,12);
            userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,MY_EVENTS_BY_DATE_TO_DAY);
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    ///////////////////////     MY_EVENTS_BY_DATE_TO_YEAR
    private BotApiMethod<?> processEventsToYear(long chatId, int userId, String answer) {
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateDay");
            YearMonth yearMonthObject = YearMonth.of(eventFilterDates.getYearFrom(), eventFilterDates.getMonthFrom());
            int daysInMonth = yearMonthObject.lengthOfMonth();
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth+ DAY_CALENDAR);
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        int day = Integer.parseInt(answer);
        eventFilterDates.setDayFrom(day);
        userEventDataCache.setEventFilterDates(userId,eventFilterDates);

        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateTo");
        int year = LocalDate.now().getYear();
        InlineKeyboardMarkup keyboardMarkup =userEventDataCache.getCalendarKeyboard(YEAR_PAST_AND_FUTURE_CALENDAR);
        if (isEmptyKeyboard(keyboardMarkup)){
            keyboardMarkup=keyboardMarkupService.getInlineKeyboardMarkupInRange(year - 5, year + 5);
            userEventDataCache.setCalendarKeyboard(YEAR_PAST_AND_FUTURE_CALENDAR,keyboardMarkup);

        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,MY_EVENTS_BY_DATE_TO_MONTH);
        return replyMessage.setReplyMarkup(keyboardMarkup);
        
    }

    ////////////////////////////////    MY_EVENTS_BY_DATE_FROM_DAY
    private BotApiMethod<?> processEventsFromDay(long chatId, int userId, String answer) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateMonth");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        int month = Integer.parseInt(answer);
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        eventFilterDates.setMonthFrom(month);
        userEventDataCache.setEventFilterDates(userId,eventFilterDates);
        
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateDay");
        YearMonth yearMonthObject = YearMonth.of(eventFilterDates.getYearFrom(), month);
        int daysInMonth = yearMonthObject.lengthOfMonth();
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(daysInMonth+ DAY_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard=keyboardMarkupService.getInlineKeyboardMarkupInRange(1,daysInMonth);
            userEventDataCache.setCalendarKeyboard(daysInMonth+ DAY_CALENDAR,calendarKeyboard);
        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,MY_EVENTS_BY_DATE_TO_YEAR);
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    ////////////////////////////////    MY_EVENTS_BY_DATE_FROM_MONTH
    private BotApiMethod<?> processEventsFromMonth(long chatId, int userId, String answer) {
        if (!StringUtils.isNumeric(answer)){
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateFrom");
            InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(YEAR_PAST_AND_FUTURE_CALENDAR);
            return replyMessage.setReplyMarkup(calendarKeyboard);
        }
        int year = Integer.parseInt(answer);
        EventFilterDates eventFilterDates = userEventDataCache.getEventFilterDates(userId);
        eventFilterDates.setYearFrom(year);
        userEventDataCache.setEventFilterDates(userId,eventFilterDates);

        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateMonth");
        InlineKeyboardMarkup calendarKeyboard = userEventDataCache.getCalendarKeyboard(MONTH_CALENDAR);
        if (isEmptyKeyboard(calendarKeyboard)){
            calendarKeyboard=keyboardMarkupService.getInlineKeyboardMarkupInRange(1,12);
            userEventDataCache.setCalendarKeyboard(MONTH_CALENDAR,calendarKeyboard);
        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,MY_EVENTS_BY_DATE_FROM_DAY);
        return replyMessage.setReplyMarkup(calendarKeyboard);
    }

    ///////////////////////////////////    MY_EVENTS_BY_DATE_FROM_YEAR

    private BotApiMethod<?> processEventsFromYear(long chatId, int userId, String answer) {
        SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, "reply.askSearchByDateFrom");
        int year = LocalDate.now().getYear();
        InlineKeyboardMarkup keyboardMarkup =userEventDataCache.getCalendarKeyboard(YEAR_PAST_AND_FUTURE_CALENDAR);
        if (isEmptyKeyboard(keyboardMarkup)){
            keyboardMarkup=keyboardMarkupService.getInlineKeyboardMarkupInRange(year - 5, year + 5);
            userEventDataCache.setCalendarKeyboard(YEAR_PAST_AND_FUTURE_CALENDAR,keyboardMarkup);

        }
        userEventDataCache.setUsersCurrentBotStateStep(userId,MY_EVENTS_BY_DATE_FROM_MONTH);
        return replyMessage.setReplyMarkup(keyboardMarkup);
    }

    ///////////////////////////////////    MY_FUTURE_EVENTS

    private BotApiMethod<?> processFutureEvents(long chatId, int userId, String answer) {
        if (answer.equals(REFRESH_FILTER)){
            List<EventResponseDto> events = eventService.getEventsByInvitedUser(userId, answer);
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, events.isEmpty()?"reply.askEventsNotFound":"reply.empty");
            InlineKeyboardMarkup inlineKeyboardMarkup=keyboardMarkupService.getEventsKeyboardMarkup(events,1);
            userEventDataCache.setUserEventsByFilter(userId,FUTURE_EVENTS,events);
            return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        List<EventResponseDto> userEventsByFilter = userEventDataCache.getUserEventsByFilter(userId, FUTURE_EVENTS);
        if (userEventsByFilter.isEmpty()){
            userEventsByFilter= eventService.getEventByTitle(userId,answer);
            userEventDataCache.setUserEventsByFilter(userId,FUTURE_EVENTS,userEventsByFilter);
        }
        String message="reply.empty";
        if (StringUtils.isNumeric(answer)){
            int id = Integer.parseInt(answer);
            String eventString=userEventsByFilter.stream().filter(e->e.getId()==id).findFirst().orElse(null).toString();
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, message,eventString);
            InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 2);
            return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
        }
        SendMessage replyMessage = 
                replyMessagesService.getReplyMessage(chatId, userEventsByFilter.isEmpty()?"reply.askEventsNotFound":message);
        InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 1);
        return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
    }

    ////////////////////////////////////   MY_EVENTS_BY_GUEST
    private BotApiMethod<?> processEventByGuest(long chatId, int userId, String answer) {
        if (answer.equals(EVENTS_BY_GUEST)){
            return replyMessagesService.getReplyMessage(chatId,"reply.askSearchByGuest");
        }
        if (answer.equals(REFRESH_FILTER)){
            List<EventResponseDto> eventByGuest = eventService.getEventsByInvitedUser(userId, answer);
            SendMessage replyMessage = eventByGuest.isEmpty()?
                    replyMessagesService.getReplyMessage(chatId,"reply.askEventsNotFound",
                             " with guest: " + answer ):
                    replyMessagesService.getReplyMessage(chatId,"reply.empty");
            InlineKeyboardMarkup inlineKeyboardMarkup=keyboardMarkupService.getEventsKeyboardMarkup(eventByGuest,1);
            userEventDataCache.setUserEventsByFilter(userId,EVENTS_BY_GUEST,eventByGuest);
            return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        List<EventResponseDto> userEventsByFilter = userEventDataCache.getUserEventsByFilter(userId, EVENTS_BY_GUEST);
        if (userEventsByFilter.isEmpty()){
            userEventsByFilter= eventService.getEventByTitle(userId,answer);
            userEventDataCache.setUserEventsByFilter(userId,EVENTS_BY_GUEST,userEventsByFilter);
        }
        String message="reply.empty";
        if (StringUtils.isNumeric(answer)){
            int id = Integer.parseInt(answer);
            String eventString=userEventsByFilter.stream().filter(e->e.getId()==id).findFirst().orElse(null).toString();
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, message,eventString);
            InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 2);
            return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
        }
        SendMessage replyMessage = userEventsByFilter.isEmpty()?
                replyMessagesService.getReplyMessage(chatId,"reply.askEventsNotFound",
                        " with guest: " + answer ):
                replyMessagesService.getReplyMessage(chatId,"reply.empty");
        InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 1);
        return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
    }

    ////////////////////////////////////   MY_EVENTS_BY_TITLE
    
    private BotApiMethod<?> processEventByTitle(long chatId, int userId, String answer) {
        if (answer.equals(EVENTS_BY_TITLE)){
            return replyMessagesService.getReplyMessage(chatId,"reply.askSearchByTitle");
        }
        if (answer.equals(REFRESH_FILTER)){
            List<EventResponseDto> eventByTitle = eventService.getEventByTitle(userId, answer);
            SendMessage replyMessage=eventByTitle.isEmpty()? 
                    replyMessagesService.getReplyMessage(chatId, "reply.askEventsNotFound", 
                            " with title: " + answer ):
                    replyMessagesService.getReplyMessage(chatId,"reply.empty");
            InlineKeyboardMarkup inlineKeyboardMarkup=keyboardMarkupService.getEventsKeyboardMarkup(eventByTitle,1);
            userEventDataCache.setUserEventsByFilter(userId,EVENTS_BY_TITLE,eventByTitle);
            return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        List<EventResponseDto> userEventsByFilter = userEventDataCache.getUserEventsByFilter(userId, EVENTS_BY_TITLE);
        if (userEventsByFilter.isEmpty()){
            userEventsByFilter= eventService.getEventByTitle(userId,answer);
            userEventDataCache.setUserEventsByFilter(userId,EVENTS_BY_TITLE,userEventsByFilter);
        }
        String message="reply.empty";
        if (StringUtils.isNumeric(answer)){
            int id = Integer.parseInt(answer);
            String eventString=
                    userEventsByFilter.stream().filter(e->e.getId()==id).findFirst().orElse(null).toString();
            SendMessage replyMessage = replyMessagesService.getReplyMessage(chatId, message,eventString);
            InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 2);
            return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
        }
        SendMessage replyMessage = userEventsByFilter.isEmpty()?
                replyMessagesService.getReplyMessage(chatId,"reply.askEventsNotFound",
                        " with title: " + answer):
                replyMessagesService.getReplyMessage(chatId, message);
        InlineKeyboardMarkup eventsKeyboardMarkup = keyboardMarkupService.getEventsKeyboardMarkup(userEventsByFilter, 1);
        return replyMessage.setReplyMarkup(eventsKeyboardMarkup);
    }

    /////////////////////////////////////  NO_STATE_STEP
    
    private BotApiMethod<?> processNoStateStep(long chatId,int userId,String suffix) {
        userEventDataCache.setUsersCurrentBotStateStep(userId,CHOOSE_EVENT_FILTER);
        SendMessage replyMessage = 
                replyMessagesService.getReplyMessage(chatId,"reply.askMyEventFilterChoose",suffix);
        InlineKeyboardMarkup inlineKeyboardMarkup=keyboardMarkupService.getFilterOptionsKeyboardMarkup();
        return replyMessage.setReplyMarkup(inlineKeyboardMarkup);
    }

    private boolean isEmptyKeyboard(InlineKeyboardMarkup keyboard) {
        return keyboard.getKeyboard()==null|| keyboard.getKeyboard().isEmpty();
    }


    @Override
    public BotState getHandlerName() {
        return GET_EVENTS;
    }
}
