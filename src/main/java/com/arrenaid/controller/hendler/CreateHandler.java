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
    private static final String CREATE_ACCEPT = "/crete_accept";
    public static final String CREATE = "/create";
    private static Count countBuffer;
    private static int scoreBuffer;
    private static String nameBuffer;
    private static boolean isFinalStep = false;

    @Autowired
    private CountRepository countRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if (message.equalsIgnoreCase(CREATE)) {
            return printEnterName(user);
        }else if(message.equalsIgnoreCase(CREATE_ACCEPT)){
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
            isFinalStep = true;
            countBuffer = new Count();
            countBuffer.setChatId(user.getChatId());
            countBuffer.setNum(1);
            countBuffer.setName(nameBuffer);
            SendMessage result = createMessageTemplate(user);
            result.setText(String.format("Name is saved as: %s%nEnter the start number to count",countBuffer.getName()));
            return List.of(result);
        }else{
            isFinalStep = false;
            countBuffer.setScore(scoreBuffer);
            countRepository.save(countBuffer);
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> inlineKeyboardButtonsRowOne;
            inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton(" Back ", String.format("%s",VIEW_START)));
            List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo = List.of(createInlineKeyboardButton(" Crete ", String.format("%s",CREATE)));
            inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne,inlineKeyboardButtonsRowTwo));
            SendMessage reply = createMessageTemplate(user);
            reply.setText(String.format("Success", user.getUsername()));
            reply.setReplyMarkup(inlineKeyboardMarkup);
            return List.of(reply);
        }
    }

    public List<PartialBotApiMethod<? extends Serializable>> checkName(User user, String message) {
        nameBuffer = message;
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton("Accept", CREATE_ACCEPT));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("You have entered: %s%nIf this is correct - press the button%nElse enter new countname", nameBuffer));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    private List<PartialBotApiMethod<? extends Serializable>> checkNumber(User user, String message) {
        try {
        scoreBuffer = Integer.parseInt(message);
        } catch (Exception e){
            e.printStackTrace();
            SendMessage reply = createMessageTemplate(user);
            reply.setText("Error: enter number");
            return List.of(reply);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton("Accept", CREATE_ACCEPT));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage result = createMessageTemplate(user);
        result.setText(String.format("You have entered: %s%nIf this is correct - press the button%nElse enter new number",scoreBuffer));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    @Override
    public State operatedBotState() {
        return State.CREATE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(CREATE_ACCEPT,CREATE);
    }
}
