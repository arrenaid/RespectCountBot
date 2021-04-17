package com.arrenaid.controller.hendler;

import com.arrenaid.controller.State;
import com.arrenaid.entity.User;
import com.arrenaid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.List;

import static com.arrenaid.controller.hendler.ViewHandler.VIEW_START;
import static com.arrenaid.util.BotUtil.createInlineKeyboardButton;
import static com.arrenaid.util.BotUtil.createMessageTemplate;

@Component
public class RegistrationHandler implements Handler{
    //Храним поддерживаемые CallBackQuery в виде констант
    public static final String NAME_ACCEPT = "/enter_name_accept";
    public static final String NAME_CHANGE = "/enter_name";
    public static final String NAME_CHANGE_CANCEL = "/enter_name_cancel";
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        // Проверяем тип полученного события
        if (message.equalsIgnoreCase(NAME_ACCEPT) || message.equalsIgnoreCase(NAME_CHANGE_CANCEL)) {
            return accept(user);
        } else if (message.equalsIgnoreCase(NAME_CHANGE)) {
            return changeName(user);
        }
        return checkName(user, message);
    }

    public List<PartialBotApiMethod<? extends Serializable>> accept(User user) {
        // Если пользователь принял имя - меняем статус и сохраняем
        user.setState(State.VIEW_COUNT);
        userRepository.save(user);

        // Создаем кнопку для начала игры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("VIEW",VIEW_START));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("Your name is saved as: %s", user.getUsername()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    public List<PartialBotApiMethod<? extends Serializable>> checkName(User user, String message) {
        // При проверке имени мы превентивно сохраняем пользователю новое имя в базе
        // идея для рефакторинга - добавить временное хранение имени
        user.setUsername(message);
        userRepository.save(user);

        // Делаем кнопку для применения изменений
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Accept", NAME_ACCEPT));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("You have entered: %s%nIf this is correct - press the button", user.getUsername()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    public List<PartialBotApiMethod<? extends Serializable>> changeName(User user) {
        // При запросе изменения имени мы меняем State
        user.setState(State.ENTER_NAME);
        userRepository.save(user);

        // Создаем кнопку для отмены операции
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Cancel", NAME_CHANGE_CANCEL));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("Your current name is: %s%nEnter new name or press the button to continue",
                user.getUsername()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    @Override
    public State operatedBotState() {
        return State.ENTER_NAME;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(NAME_ACCEPT, NAME_CHANGE, NAME_CHANGE_CANCEL);
    }
}
