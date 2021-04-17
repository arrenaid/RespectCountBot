package com.arrenaid.controller;

import com.arrenaid.controller.hendler.Handler;
import com.arrenaid.entity.User;
import com.arrenaid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Component
public class UpdateReceiver {
    private final List<Handler> handlers;
    private final UserRepository userRepository;


    public UpdateReceiver(List<Handler> handlers,UserRepository userRepository) {
        this.handlers = handlers;
        this.userRepository = userRepository;
    }

    public List<PartialBotApiMethod<? extends Serializable>> handle (Update update){
        try {
            // Проверяем, если Update - сообщение с текстом
            if (isMessageWithText(update)) {
                // Получаем Message из Update
                final Message message = update.getMessage();
                // Получаем айди чата с пользователем
                final int chatId = Math.toIntExact(message.getFrom().getId());

                // Просим у репозитория пользователя. Если такого пользователя нет - создаем нового и возвращаем его.
                // Как раз на случай нового пользователя мы и сделали конструктор с одним параметром в классе User
                final User user = userRepository.getByChatId(chatId)
                        .orElseGet(() -> userRepository.save(new User(State.START,chatId)));
                // Ищем нужный обработчик и возвращаем результат его работы
                return getHandlerByState(user.getState()).handle(user, message.getText());

            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                final int chatId = Math.toIntExact(callbackQuery.getFrom().getId());
                final User user = userRepository.getByChatId(chatId)
                        .orElseGet(() -> userRepository.save(new User(State.START,chatId)));

                return getHandlerByCallBackQuery(callbackQuery.getData()).handle(user, callbackQuery.getData());
            }

            throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException e) {
            return Collections.emptyList();
        }
    }

    private Handler getHandlerByState(State state) {
        return handlers.stream()
                .filter(h -> h.operatedBotState() != null)
                .filter(h -> h.operatedBotState().equals(state))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private Handler getHandlerByCallBackQuery(String query) {
        return handlers.stream()
                .filter(h -> h.operatedCallBackQuery().stream()
                        .anyMatch(query::startsWith))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }
    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }
}
