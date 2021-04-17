package com.arrenaid.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MainBotController extends TelegramLongPollingBot {

    @Value("${telegrambot.botUsername}")
    private String botUsername;

    @Value("${telegrambot.botToken}")
    private String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message =new SendMessage();
        message.setText("Hi!");
        message.setChatId(update.getMessage().getChatId().toString());
        try {
            execute(message);
        } catch (TelegramApiException e) {
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
