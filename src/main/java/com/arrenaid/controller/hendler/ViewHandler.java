package com.arrenaid.controller.hendler;

import com.arrenaid.controller.State;
import com.arrenaid.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


import static com.arrenaid.util.BotUtil.createMessageTemplate;
@Component
public class ViewHandler implements Handler{

    public static final String VIEW_START = "/view_start";

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage emptyMessage = createMessageTemplate(user);
        emptyMessage.setText("VIEW is EMPTY");
        return List.of(emptyMessage);
    }

    @Override
    public State operatedBotState() {
        return State.VIEW_COUNT;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(VIEW_START);
    }
}
