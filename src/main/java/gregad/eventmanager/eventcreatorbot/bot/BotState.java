package gregad.eventmanager.eventcreatorbot.bot;

/**
 * @author Greg Adler
 */
public enum BotState {
    ASK_TITLE,
    ASK_DESCRIPTION,
    ASK_DATE,
    ASK_TIME,
    ASK_TEMPLATE,
    FILLING_EVENT_FORM,
    EVENT_FILLED,
    GET_EVENTS,
    MY_EVENTS_BY_TITLE,
    MY_EVENTS_BY_GUEST,
    MY_EVENTS_BY_DATES,
    MY_FUTURE_EVENTS,
    STATISTICS,
    SHOW_MAIN_MENU,
    SHOW_HELP_MENU;
}
