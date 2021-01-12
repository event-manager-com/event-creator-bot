package gregad.eventmanager.eventcreatorbot.bot.messaging.utils;

import gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants;
import gregad.eventmanager.eventcreatorbot.dto.EventResponseDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregad.eventmanager.eventcreatorbot.bot.constants.BotConstants.*;

/**
 * @author Greg Adler
 */
@Component
public class KeyboardMarkupService {


    public InlineKeyboardMarkup getInlineKeyboardMarkupInRange(int from, int to) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            keyboardButtonsRow.add(getButton(i + "", i + ""));
            if (i % 6 == 0) {
                buttons.add(keyboardButtonsRow);
                keyboardButtonsRow = new ArrayList<>();
            }
        }
        buttons.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardButton getButton(String text, String callbackValue) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        return button.setCallbackData(callbackValue);
    }
///////////////////////////////////////////////////////////////////////////////////////


    public InlineKeyboardMarkup getTemplatesInlineKeyboardMarkup(String myUrl, String eventType) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        String url = myUrl + "/template/";
        File dir = new File(PATH_TO_TEMPLATES + eventType + "\\");
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String imageName = files[i].getName();
            InlineKeyboardButton valueButton = new InlineKeyboardButton();
            InlineKeyboardButton linkButton = new InlineKeyboardButton();
            valueButton.setText(i + "");
            valueButton.setCallbackData(imageName);
            linkButton.setText("Click to see " + imageName);
            linkButton.setUrl(url + eventType + "/" + imageName);
            row.add(valueButton);
            row.add(linkButton);
            buttons.add(row);
            row = new ArrayList<>();
        }
        return new InlineKeyboardMarkup().setKeyboard(buttons);

    }
    /////////////////////////////////////////////////////////////////////////////////////////

    public InlineKeyboardMarkup getEventTypesInlineKeyboardMarkup() {
        File[] files = new File(PATH_TO_TEMPLATES).listFiles();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (File file : files) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(file.getName());
            button.setCallbackData(file.getName());
            row.add(button);
            buttons.add(row);
            row = new ArrayList<>();
        }
        return new InlineKeyboardMarkup().setKeyboard(buttons);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public InlineKeyboardMarkup getFilterOptionsKeyboardMarkup() {
        List<String> options = Arrays.asList(EVENTS_BY_TITLE, FUTURE_EVENTS, EVENTS_BETWEEN_DATES, EVENTS_BY_GUEST);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (String option : options) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(option);
            button.setCallbackData(option);
            row.add(button);
            buttons.add(row);
            row = new ArrayList<>();
        }
        return inlineKeyboardMarkup.setKeyboard(buttons);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public InlineKeyboardMarkup getEventsKeyboardMarkup(List<EventResponseDto> events, int buttonsInLine) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            if (i > 0 && i % buttonsInLine == 0) {
                buttons.add(row);
                row = new ArrayList<>();
            }
            EventResponseDto eventResponseDto = events.get(i);
            InlineKeyboardButton button = getButton(eventResponseDto.getTitle(), eventResponseDto.getId() + "");
            row.add(button);
        }
        if (!row.isEmpty()) {
            buttons.add(row);
        }
        if (buttonsInLine == 1) {
            getButton(REFRESH_FILTER, REFRESH_FILTER);
            buttons.add(row);
        }
        return inlineKeyboardMarkup.setKeyboard(buttons);
    }

}
