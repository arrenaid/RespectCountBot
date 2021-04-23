package com.arrenaid.controller.hendler;

import com.arrenaid.controller.State;
import com.arrenaid.entity.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;

public interface Handler{
    List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message);
    State operatedBotState();
    List<String> operatedCallBackQuery();
}
