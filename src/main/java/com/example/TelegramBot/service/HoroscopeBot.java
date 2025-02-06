package com.example.TelegramBot.service;

import com.example.TelegramBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

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

            switch (messageText) {
                case "/start":
                    sendWelcomeMessage(chatId);
                    break;

                case "\uD83D\uDD2E Гороскоп":
                    sendHoroscope(chatId);
                    break;

                case "\uD83D\uDCA1 Мотивация":
                    sendMotivation(chatId);
                    break;

                default:
                    sendTextMessage(chatId, "Пожалуйста, выберите один из доступных вариантов ⬇");
                    break;
            }
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "Welcome to Bolshov Personal Bot!\n\nВыбери, что тебе интересно:";
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeText);
        message.setReplyMarkup(getMainMenuKeyboard());
        sendMessage(message);
    }

    private ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("🔮 Гороскоп");
        row.add("💡 Мотивация");

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void sendHoroscope(long chatId) {
        String horoscope = "Ваш гороскоп на сегодня: 🌟 ... ";
        sendTextMessage(chatId, horoscope);
    }

    private void sendMotivation(long chatId) {
        String motivation = "Сегодняшняя мотивация: 🚀 ...";
        sendTextMessage(chatId, motivation);
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        sendMessage(message);
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
