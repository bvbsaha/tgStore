package org.sabb.controller;

import lombok.extern.log4j.Log4j;
import org.sabb.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils; //внедрение зависимости messageUtils

    public UpdateController(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }


    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update){       //первичная валидация входящих данных
         if (update == null){
            log.error("Recieved update is null");
            return;
         }
         if (update.getMessage() != null){
             distributeMessageByType(update);
         } else {
            log.error("Recieved unsupportde message type" + update);
         }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.getText() != null){
            processTextMessage(update);
        } else if (message.getDocument() != null) {
            processDocMessage(update);
        } else if (message.getPhoto() != null) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) { //ответ о том, что данное сообщение не поддерживается
       var sendMessage = messageUtils.generateSendMessageWithText(update,"Данный тип сообщений не поддерживается");
        setView(sendMessage);
    }
    private void setView(SendMessage sendMessage){
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processPhotoMessage(Update update) {
    }

    private void processDocMessage(Update update) {
    }

    private void processTextMessage(Update update) {
    }
}
