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
import java.util.Collections;
import java.util.List;

import static com.arrenaid.controller.hendler.CreateHandler.CREATE;
import static com.arrenaid.controller.hendler.RegistrationHandler.NAME_CHANGE;
import static com.arrenaid.controller.hendler.ViewHandler.VIEW_START;
import static com.arrenaid.util.BotUtil.createInlineKeyboardButton;
import static com.arrenaid.util.BotUtil.createMessageTemplate;

@Component
public class HelpHandler implements Handler{
    public static final String HELP = "/help";
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if (message.equalsIgnoreCase(HELP)){
            return help(user);
        }else if(message.equalsIgnoreCase(CREATE)) {
            return helpCreate(user);
        }
        return help(user);
//        switch (message){
//            case NAME_CHANGE:
//                return null;
//            case VIEW_START:
//                return helpView(user);
//            case CREATE:
//                return helpCreate(user);
//            default:
//                return help(user);
//        }

    }

    private List<PartialBotApiMethod<? extends Serializable>> helpCreate(User user) {
        user.setState(State.CREATE);
        userRepository.save(user);
        SendMessage reply = createMessageTemplate(user);
        reply.setText("Tell me countname");
        return List.of(reply);
    }

    private List<PartialBotApiMethod<? extends Serializable>> helpView(User user) {
        user.setState(State.VIEW_COUNT);
        userRepository.save(user);
        SendMessage reply = createMessageTemplate(user);
        reply.setText("All that was found:");
        return List.of(reply);
    }

    public List<PartialBotApiMethod<? extends Serializable>> help(User user){
        // Создаем кнопку для смены имени, вывода всех подсчетов, и добавления нового
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Change name", NAME_CHANGE),
                createInlineKeyboardButton("View count", VIEW_START),
                createInlineKeyboardButton("Create count", CREATE));

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
        return List.of(HELP);
    }
}
