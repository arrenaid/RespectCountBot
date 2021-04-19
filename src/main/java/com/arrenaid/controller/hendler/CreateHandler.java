package com.arrenaid.controller.hendler;

import com.arrenaid.controller.State;
import com.arrenaid.entity.Count;
import com.arrenaid.entity.User;
import com.arrenaid.repository.CountRepository;
import com.arrenaid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.arrenaid.controller.hendler.HelpHandler.HELP;
import static com.arrenaid.controller.hendler.ViewHandler.VIEW_START;
import static com.arrenaid.util.BotUtil.createInlineKeyboardButton;
import static com.arrenaid.util.BotUtil.createMessageTemplate;

@Component
public class CreateHandler implements Handler{
    private static final String ACCEPT = "/crete_accept";
    public static final String CREATE = "/create";
    private static Count countBuffer;
    private static boolean isFinalStep = false;

    @Autowired
    private CountRepository countRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if (message.equalsIgnoreCase(CREATE)) {
            return printEnterName(user);
        }else if(message.equalsIgnoreCase(ACCEPT)){
            return accept(user);
        }else if(message.equalsIgnoreCase(HELP)){
            return backToHelp(user);
        }
        return messageHandler(user, message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> backToHelp(User user) {
        user.setState(State.NONE);
        userRepository.save(user);
        return Collections.emptyList();
    }

    private List<PartialBotApiMethod<? extends Serializable>> messageHandler(User user, String message) {
        if(!isFinalStep){
            return checkName(user, message);
        }else {
            return checkNumber(user, message);
        }
    }

    public List<PartialBotApiMethod<? extends Serializable>> printEnterName(User user) {
        user.setState(State.CREATE);
        userRepository.save(user);
        SendMessage message = createMessageTemplate(user);
        message.setText("In order to create a new count enter the countname");
        return List.of(message);
    }

    public List<PartialBotApiMethod<? extends Serializable>> accept(User user) {
        if (!isFinalStep) {
            // Если пользователь принял имя - меняем статус и сохраняем
            isFinalStep = true;
            SendMessage result = createMessageTemplate(user);
            result.setText(String.format("Name is saved as: %s%nEnter the start number to count",countBuffer.getName()));
            return List.of(result);
        }else{
            countRepository.save(countBuffer);
            user.setState(State.VIEW_COUNT);
            userRepository.save(user);
            SendMessage result = createMessageTemplate(user);
            result.setText(String.format("Success", user.getUsername()));
            return List.of(result);
        }
    }

    public List<PartialBotApiMethod<? extends Serializable>> checkName(User user, String message) {
        // При проверке имени мы превентивно сохраняем пользователю новое имя в базе
        // идея для рефакторинга - добавить временное хранение имени
        if(countBuffer == null) {
            countBuffer = new Count();
            countBuffer.setChatId(user.getChatId());
            countBuffer.setNum(1);
        }
        countBuffer.setName(message);
        // Делаем кнопку для применения изменений
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton("Accept", ACCEPT));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("You have entered: %s%nIf this is correct - press the button%nElse enter new countname", countBuffer.getName()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    private List<PartialBotApiMethod<? extends Serializable>> checkNumber(User user, String message) {
        countBuffer.setScore(Integer.parseInt(message));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton("Accept", ACCEPT));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("You have entered: %s%nIf this is correct - press the button%nElse enter new number", String.valueOf(countBuffer.getScore())));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    @Override
    public State operatedBotState() {
        return State.CREATE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(ACCEPT,CREATE);
    }
}
