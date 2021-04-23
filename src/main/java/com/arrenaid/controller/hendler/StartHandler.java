package com.arrenaid.controller.hendler;

import com.arrenaid.controller.State;
import com.arrenaid.entity.User;
import com.arrenaid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.arrenaid.util.BotUtil.createMessageTemplate;

@Component
public class StartHandler implements Handler {
    @Value("${telegrambot.botUsername}")
    private String botUsername;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage welcomeMessage = createMessageTemplate(user);
        welcomeMessage.setText(String.format("Hey! I'm *%s*%nI'm here to help you count something. for example, the number of your hits to the target", botUsername));
        SendMessage registrationMessage = createMessageTemplate(user);
        registrationMessage.setText("To start tell me your name");
        user.setState(State.ENTER_NAME);
        userRepository.save(user);
        return List.of(welcomeMessage, registrationMessage);
    }

    @Override
    public State operatedBotState() {
        return State.START;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
