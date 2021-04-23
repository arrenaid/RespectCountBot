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
import java.util.*;


import static com.arrenaid.controller.hendler.ChangeHandler.CHANGE;
import static com.arrenaid.controller.hendler.CreateHandler.CREATE;
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
    public static final String PLUS = "/plus";
    public static final String MINUS = "/minus";

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if(message.equalsIgnoreCase(HELP)){
            return backToHelp(user);
        }else
            if(message.startsWith(PLUS)){
            return plusOrMinus(user, message,true);
        }else if(message.startsWith(MINUS)){
            return plusOrMinus(user, message,false);
        }
        else if(message.equalsIgnoreCase(VIEW_START)){
            return view(user, message, true);
        }
        return view(user, message,false);
    }

    private List<PartialBotApiMethod<? extends Serializable>> plusOrMinus(User user, String message, boolean isPlus) {
        Optional<Count> countFindResult = countRepository.findById(Integer.parseInt(getWordFromMessage(message,1)));
        Count count = countFindResult.get();
        if(count == null) {
            SendMessage emptyMessage = createMessageTemplate(user);
            emptyMessage.setText(String.format("Not found count"));
            return List.of(emptyMessage);
        }
        int currentScore = count.getScore();
        int currentNum = count.getNum();
        if(isPlus){
            count.setScore(currentScore + 1);
        }else {
            count.setScore(currentScore - 1);
        }
        count.setNum(currentNum + 1);
        countRepository.save(count);
        SendMessage replyPlus = createMessageTemplate(user);
        replyPlus.setText(String.format("Success plus(minus)"));
        SendMessage replyView = generateViewAndButton(user, count);
        return List.of(replyPlus,replyView);
    }

    private List<PartialBotApiMethod<? extends Serializable>> backToHelp(User user) {
        user.setState(State.NONE);
        userRepository.save(user);
        return Collections.emptyList();
    }

    private List<PartialBotApiMethod<? extends Serializable>> view(User user, String message, boolean isAll) {
        List<Count> countList = new ArrayList<Count>();
        if(isAll) {
            countList = countRepository.getAllByChatId(user.getChatId());
        }else {
            countList = countRepository.findByNameContainingIgnoreCase(message);
        }
        SendMessage reply = createMessageTemplate(user);
        ArrayList<SendMessage> viewMessage = new ArrayList<SendMessage>();
        if(countList.isEmpty()) {
            reply.setText("Nothing");
        }else {
            user.setState(State.VIEW_COUNT);
            userRepository.save(user);
            reply.setText("All that was found:");
            countList.forEach(element -> {
                try {
                    viewMessage.add(generateViewAndButton(user, element));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo = List.of(
                createInlineKeyboardButton("Create new count",CREATE));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowTwo));
        reply.setReplyMarkup(inlineKeyboardMarkup);
        SendMessage [] array = new SendMessage[viewMessage.size() + 1];
        array[0]=reply;
        for(int i = 0; i < viewMessage.size(); i++){
            array[i + 1] = viewMessage.get(i);
        }
        return List.of(array);
    }

    private SendMessage generateViewAndButton(User user, Count count) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne;
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(" More ");
        button.setCallbackData(String.format("%s %d",CHANGE, count.getId()));
        inlineKeyboardButtonsRowOne = List.of(button);
        List<InlineKeyboardButton> inlineKeyboardButtonsRowTwo;
        inlineKeyboardButtonsRowTwo = List.of(createInlineKeyboardButton(" + ", String.format("%s %d", PLUS, count.getId())),
                createInlineKeyboardButton(" - ", String.format("%s %d", MINUS, count.getId())));
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne,inlineKeyboardButtonsRowTwo));
        SendMessage result = createMessageTemplate(user);
        result.setText(String.format( "Name: %s%nScore: %s",count.getName(), count.getScore()));
        result.setReplyMarkup(inlineKeyboardMarkup);
        return result;
    }

    public String getWordFromMessage(String message, int index) {
        String[] array = message.split(" ");
        return array[index];
    }

    @Override
    public State operatedBotState() {
        return State.VIEW_COUNT;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(VIEW_START,PLUS,MINUS);
    }
}
