package gregad.eventmanager.eventcreatorbot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Greg Adler
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {
    private long id;
    private UserDto owner;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
    @JsonFormat(pattern = "KK:mm a")
    private LocalTime eventTime;
    private String imageUrl;
    private String telegramChannelRef;
    private List<UserDto> invited;
    private List<Message> correspondences;

}
