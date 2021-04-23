package com.arrenaid.controller.hendler;

import com.arrenaid.controller.MainBotController;
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
import java.util.Optional;

import static com.arrenaid.controller.hendler.HelpHandler.HELP;
import static com.arrenaid.controller.hendler.ViewHandler.VIEW_START;
import static com.arrenaid.util.BotUtil.createInlineKeyboardButton;
import static com.arrenaid.util.BotUtil.createMessageTemplate;

@Component
public class ChangeHandler implements Handler{
    public static final String CHANGE = "/change";
    public static final String NAME = "/name";
    public static final String SCORE = "/score";
    public static final String DELETE = "/delete";
    public static final String CHANGE_ACCEPT = "/accept_change";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CountRepository countRepository;
    private static Count countBuffer;
    private static String nameBuffer = null;
    private static int scoreBuffer = 0;
    private static boolean isChangeName = false;
    private static boolean isChangeScore= false;


    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if (message.startsWith(CHANGE)) {
            return start(user, message);
        } else if (message.startsWith(DELETE)) {
            return deleteCount(user, message);
        } else if (message.startsWith(NAME) || message.startsWith(SCORE)) {
            return changeValue(user, message);
        } else if (message.equalsIgnoreCase(HELP)) {
            return backToHelp(user);
        } else if (message.equalsIgnoreCase(CHANGE_ACCEPT)) {
            return acceptChanges(user, message);
        }
        return check(user, message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> acceptChanges(User user, String message) {
        if(isChangeName){
            isChangeName = false;
            countBuffer.setName(nameBuffer);
        }
        if(isChangeScore){
            isChangeScore =false;
            countBuffer.setScore(scoreBuffer);
        }
        countRepository.save(countBuffer);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton(" View ", String.format("%s",VIEW_START)));
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo = List.of(createInlineKeyboardButton(" Change ", String.format("%s",CHANGE)));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne,inlineKeyboardButtonsRowTwo));
        SendMessage reply = createMessageTemplate(user);
        reply.setText("Change success");
        reply.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(reply);
    }

    private List<PartialBotApiMethod<? extends Serializable>> check(User user, String message) {
        if(isChangeName){
            nameBuffer = message;
        }else if(isChangeScore){
            try {
                scoreBuffer = Integer.parseInt(message);
            } catch (Exception e){
                e.printStackTrace();
                SendMessage reply = createMessageTemplate(user);
                reply.setText("Error: enter number");
                return List.of(reply);
            }
        }else {
            return start(user, message  + " " + countBuffer.getId());
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton("Accept", CHANGE_ACCEPT));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage reply = createMessageTemplate(user);
        if(isChangeName) {
            reply.setText(String.format("You have entered: %s%nIf this is correct - press the button", nameBuffer));
        }
        if(isChangeScore){
            reply.setText(String.format("You have entered: %s%nIf this is correct - press the button", scoreBuffer));
        }
        reply.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(reply);
    }

    private List<PartialBotApiMethod<? extends Serializable>> changeValue(User user, String message) {
        isChangeName = message.startsWith(NAME);
        isChangeScore = message.startsWith(SCORE);
        SendMessage reply = createMessageTemplate(user);
        reply.setText("Enter new value");
        return List.of(reply);
    }



    private List<PartialBotApiMethod<? extends Serializable>> deleteCount(User user, String message) {
        Count count = countBuffer;
        countRepository.delete(count);
        user.setState(State.VIEW_COUNT);
        userRepository.save(user);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne;
        inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton(" Back ", String.format("%s",VIEW_START)));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
        SendMessage reply = createMessageTemplate(user);
        reply.setText("Delete success");
        reply.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(reply);
    }

    private List<PartialBotApiMethod<? extends Serializable>> start(User user, String message) {
        countBuffer = countFind(message, 1);
        if(countBuffer == null){
            return null;
        }
        user.setState(State.CHANGE);
        userRepository.save(user);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne;
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo;
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTree;
        List<InlineKeyboardButton> inlineKeyboardButtonsRowFour;
        inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton(" Delete ", String.format("%s %d", DELETE, countBuffer.getId())));
        inlineKeyboardButtonsRowTwo = List.of(createInlineKeyboardButton(" Change name ", String.format("%s %d", NAME, countBuffer.getId())));
        inlineKeyboardButtonsRowTree = List.of(createInlineKeyboardButton(" Change score ", String.format("%s %d", SCORE, countBuffer.getId())));
        inlineKeyboardButtonsRowFour = List.of(createInlineKeyboardButton(" Back ", String.format("%s",VIEW_START)));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne,inlineKeyboardButtonsRowTwo,
                inlineKeyboardButtonsRowTree,inlineKeyboardButtonsRowFour));
        SendMessage result = createMessageTemplate(user);
        result.setText(String.format( "Name: %s%nCount: %s",countBuffer.getName(), countBuffer.getScore()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return List.of(result);
    }

    private Count countFind(String message, int index) {
        String[] array = message.split(" ");
        Optional<Count> countFindResult = countRepository.findById(Integer.parseInt(array[index]));
        return countFindResult.get();
    }

    private List<PartialBotApiMethod<? extends Serializable>> backToHelp(User user) {
        user.setState(State.NONE);
        userRepository.save(user);
        return Collections.emptyList();
    }

    @Override
    public State operatedBotState() {
        return State.CHANGE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(CHANGE, DELETE, NAME, SCORE, CHANGE_ACCEPT);
    }
}
