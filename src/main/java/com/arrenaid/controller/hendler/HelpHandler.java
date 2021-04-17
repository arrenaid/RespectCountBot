package com.arrenaid.controller.hendler;

import com.arrenaid.controller.State;
import com.arrenaid.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.arrenaid.controller.hendler.RegistrationHandler.NAME_CHANGE;
import static com.arrenaid.util.BotUtil.createInlineKeyboardButton;
import static com.arrenaid.util.BotUtil.createMessageTemplate;

@Component
public class HelpHandler implements Handler{

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        // Создаем кнопку для смены имени
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Change name", NAME_CHANGE));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        SendMessage result = createMessageTemplate(user);
        result.setText(String.format( "You've asked for help %s? Here it comes!", user.getUsername()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);

    }

    @Override
    public State operatedBotState() {
        return State.NONE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
