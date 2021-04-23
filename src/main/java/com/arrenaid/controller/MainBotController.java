package com.arrenaid.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

@Component
public class MainBotController extends TelegramLongPollingBot {

    @Value("${telegrambot.botUsername}")
    private String botUsername;

    @Value("${telegrambot.botToken}")
    private String botToken;

    private final UpdateReceiver updateReceiver;

    public MainBotController(UpdateReceiver updateReceiver) {
        this.updateReceiver = updateReceiver;
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<PartialBotApiMethod<? extends Serializable>> messagesToSent = updateReceiver.handle(update);
        if(!messagesToSent.isEmpty() && messagesToSent != null){
            messagesToSent.forEach(response ->{
                if(response instanceof SendMessage){
                    executeWithExceptionCheck((SendMessage) response);
                }
            });
        }
    }

    private void executeWithExceptionCheck(SendMessage message) {
        try{
            execute(message);
        } catch (TelegramApiException e){
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
