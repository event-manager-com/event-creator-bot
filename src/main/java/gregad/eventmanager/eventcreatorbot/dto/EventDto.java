package gregad.eventmanager.eventcreatorbot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Greg Adler
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long id;
    private UserDto owner;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @JsonFormat(pattern = "hh:mm a")
    private LocalTime eventTime;
    private String imageUrl;
    private String telegramChannelRef;

}
