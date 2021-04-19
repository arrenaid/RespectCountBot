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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import static com.arrenaid.controller.hendler.HelpHandler.HELP;
import static com.arrenaid.util.BotUtil.createInlineKeyboardButton;
import static com.arrenaid.util.BotUtil.createMessageTemplate;
@Component
public class ViewHandler implements Handler{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CountRepository countRepository;

    public static final String VIEW_START = "/view";

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if(message.equalsIgnoreCase(HELP)){
            return backToHelp(user);
        }
//        else if(message.equalsIgnoreCase(VIEW_START)){
//            return viewStart(user);
//        }
        return view(user,message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> viewStart(User user) {
        user.setState(State.VIEW_COUNT);
        userRepository.save(user);
        SendMessage replyStart = createMessageTemplate(user);
        replyStart.setText("All that was found:");
        return Collections.emptyList();
    }

    private List<PartialBotApiMethod<? extends Serializable>> backToHelp(User user) {
        user.setState(State.NONE);
        userRepository.save(user);
        return Collections.emptyList();
    }

    private List<PartialBotApiMethod<? extends Serializable>> view(User user, String message) {
//        user.setState(State.VIEW_COUNT);
//        userRepository.save(user);
        SendMessage replyStart = createMessageTemplate(user);
        replyStart.setText("All that was found:");
        List<Count> countList = countRepository.getAllByChatId(user.getChatId());
        if(countList.isEmpty()) {
            SendMessage emptyMessage = createMessageTemplate(user);
            emptyMessage.setText("Nothing");
//            user.setState(State.NONE);
//            userRepository.save(user);
            return List.of(emptyMessage);
        }
        ArrayList<SendMessage> viewMessage = new ArrayList<SendMessage>();
        countList.forEach(element->{
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> inlineKeyboardButtonsRowOne;
            inlineKeyboardButtonsRowOne = List.of(createInlineKeyboardButton(" + ", "/plus"),
                    createInlineKeyboardButton(" - ", "/minus"));
            inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));
            SendMessage msg = createMessageTemplate(user);
            msg.setText(String.format( "Name: %s%nCount: %s",element.getName(),String.valueOf(element.getScore())));
            msg.setReplyMarkup(inlineKeyboardMarkup);
            try {
                viewMessage.add(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        SendMessage [] array = new SendMessage[viewMessage.size() + 1];
        array[0]=replyStart;
        for(int i = 0; i <viewMessage.size();i++){
            array[i+1]=viewMessage.get(i);
        }
        return List.of(array);
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
