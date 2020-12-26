package gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model;

import com.fasterxml.jackson.annotation.JsonFormat;
import gregad.eventmanager.eventcreatorbot.dto.UserDto;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Greg Adler
 */
@Data
public class EventModel {
    private UserDto owner;
    private String title;
    private String description;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private String eventType;
    private String imageUrl;
    private String telegramChannelRef;
}
