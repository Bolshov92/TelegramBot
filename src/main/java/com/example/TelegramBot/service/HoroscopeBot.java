package com.example.TelegramBot.service;

import com.example.TelegramBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class HoroscopeBot extends TelegramLongPollingBot {

    private final BotConfig config;

    public HoroscopeBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            } else {
                sendTextMessage(chatId, "Please type /start to begin.");
            }
        }
    }

    public void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you! I'll be sending you your personal horoscope every day.";
        sendTextMessage(Long.parseLong(String.valueOf(chatId)), answer);
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
