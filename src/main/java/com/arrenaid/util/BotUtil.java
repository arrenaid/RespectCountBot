package com.arrenaid.util;

import com.arrenaid.entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class BotUtil {
    public static SendMessage createMessageTemplate(User user){
        return createMessageTemplate(String.valueOf(user.getChatId()));
    }
    // Создаем шаблон SendMessage с включенным Markdown
    public static SendMessage createMessageTemplate(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableMarkdown(true);
        return message;
    }
    public static InlineKeyboardButton createInlineKeyboardButton(String text, String command){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(command);
        return button;
    }
}
