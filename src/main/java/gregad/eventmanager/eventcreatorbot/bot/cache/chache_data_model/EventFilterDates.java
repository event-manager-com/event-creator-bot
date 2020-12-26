package gregad.eventmanager.eventcreatorbot.bot.cache.chache_data_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Greg Adler
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFilterDates {
    private int yearFrom;
    private int monthFrom;
    private int dayFrom;
    private int yearTo;
    private int monthTo;
    private int dayTo;
}
