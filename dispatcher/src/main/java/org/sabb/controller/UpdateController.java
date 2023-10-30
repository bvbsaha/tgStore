package org.sabb.controller;

import lombok.extern.log4j.Log4j;
import org.sabb.service.UpdateProducer;
import org.sabb.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.sabb.RabbitQueue.*;

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;    //внедрение зависимостей messageUtils и updateProducer
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }

    /** Метод для первичной валидации входящих данных
     * @param update
     */
    public void processUpdate(Update update){
         if (update == null){
            log.error("Recieved update is null");
            return;
         }
         if (update.getMessage() != null){
             distributeMessageByType(update);
         } else {
            log.error("Recieved unsupported message type " + update);
         }
    }

    /** Метод определяет тип входящего сообщения
     * @param update
     */
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

    /** Метод возвращает сообщение о том, что сообщение не поддерживается
     * @param update
     */
    private void setUnsupportedMessageTypeView(Update update) {
       var sendMessage = messageUtils.generateSendMessageWithText(update,
               "Данный тип сообщений не поддерживается");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage){
        telegramBot.sendAnswerMessage(sendMessage);
    }

    /** Метод возвращает сообщение о том, что файл получен
     * @param update данные из телеграма
     */
    private void setFileIsRecieved(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update,
                "Обработка файла");
        setView(sendMessage);
    }

    /** Метод для передачи разных типов сообщений в соответствующую очередь
     * @param update данные из телеграма
     */
    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE,update);
        setFileIsRecieved(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE,update);
        setFileIsRecieved(update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE,update);
    }
}
