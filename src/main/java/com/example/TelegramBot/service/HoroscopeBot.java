package com.example.TelegramBot.service;

import com.example.TelegramBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class HoroscopeBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final HoroscopeService horoscopeService;
    private final TranslationService translationService;

    private final Map<Long, String> userLanguageMap = new HashMap<>();

    public HoroscopeBot(BotConfig config, HoroscopeService horoscopeService, TranslationService translationService) {
        this.config = config;
        this.horoscopeService = horoscopeService;
        this.translationService = translationService;
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
            String userName = update.getMessage().getChat().getFirstName();

            if (!userLanguageMap.containsKey(chatId)) {
                handleLanguageSelection(chatId, messageText, userName);
            } else {
                handleUserCommands(chatId, messageText);
            }
        }
    }

    private void handleLanguageSelection(long chatId, String messageText, String userName) {
        switch (messageText) {
            case "/start":
                sendLanguageSelection(chatId);
                break;

            case "🇷🇺 Русский":
                userLanguageMap.put(chatId, "ru");
                sendWelcomeMessage(chatId, userName, "ru");
                break;

            case "🇬🇧 English":
                userLanguageMap.put(chatId, "en");
                sendWelcomeMessage(chatId, userName, "en");
                break;

            default:
                sendTextMessage(chatId, "Пожалуйста, выберите язык / Please select a language:");
                sendLanguageSelection(chatId);
                break;
        }
    }

    private void sendLanguageSelection(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🌍 Выберите язык / Choose a language:");
        message.setReplyMarkup(getLanguageKeyboard());
        sendMessage(message);
    }

    private ReplyKeyboardMarkup getLanguageKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("🇷🇺 Русский");
        row.add("🇬🇧 English");

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void sendWelcomeMessage(long chatId, String userName, String language) {
        String welcomeText = language.equals("ru") ?
                String.format("Привет, %s! 👋 Добро пожаловать в бот гороскопов!\n\nВыберите ваш знак зодиака:", userName) :
                String.format("Hello, %s! 👋 Welcome to the Horoscope Bot!\n\nChoose your zodiac sign:", userName);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeText);
        message.setReplyMarkup(getZodiacKeyboard(language));
        sendMessage(message);
    }

    private void handleUserCommands(long chatId, String messageText) {
        String userLanguage = userLanguageMap.getOrDefault(chatId, "ru");

        switch (messageText) {
            case "🔮 Гороскоп":
                sendZodiacKeyboard(chatId, userLanguage);
                break;

            case "♈ Овен":
            case "♉ Телец":
            case "♊ Близнецы":
            case "♋ Рак":
            case "♌ Лев":
            case "♍ Дева":
            case "♎ Весы":
            case "♏ Скорпион":
            case "♐ Стрелец":
            case "♑ Козерог":
            case "♒ Водолей":
            case "♓ Рыбы":
            case "♈ Aries":
            case "♉ Taurus":
            case "♊ Gemini":
            case "♋ Cancer":
            case "♌ Leo":
            case "♍ Virgo":
            case "♎ Libra":
            case "♏ Scorpio":
            case "♐ Sagittarius":
            case "♑ Capricorn":
            case "♒ Aquarius":
            case "♓ Pisces":
                sendHoroscope(chatId, messageText);
                break;

            default:
                sendTextMessage(chatId, userLanguage.equals("ru") ? "Пожалуйста, выберите один из доступных вариантов ⬇" :
                        "Please choose one of the available options ⬇");
                break;
        }
    }

    private void sendZodiacKeyboard(long chatId, String language) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(language.equals("ru") ? "Выберите свой знак зодиака:" : "Choose your zodiac sign:");
        message.setReplyMarkup(getZodiacKeyboard(language));
        sendMessage(message);
    }

    private ReplyKeyboardMarkup getZodiacKeyboard(String language) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        if ("en".equals(language)) {
            keyboard.add(createRow("♈ Aries", "♉ Taurus", "♊ Gemini"));
            keyboard.add(createRow("♋ Cancer", "♌ Leo", "♍ Virgo"));
            keyboard.add(createRow("♎ Libra", "♏ Scorpio", "♐ Sagittarius"));
            keyboard.add(createRow("♑ Capricorn", "♒ Aquarius", "♓ Pisces"));
        } else {
            keyboard.add(createRow("♈ Овен", "♉ Телец", "♊ Близнецы"));
            keyboard.add(createRow("♋ Рак", "♌ Лев", "♍ Дева"));
            keyboard.add(createRow("♎ Весы", "♏ Скорпион", "♐ Стрелец"));
            keyboard.add(createRow("♑ Козерог", "♒ Водолей", "♓ Рыбы"));
        }

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private KeyboardRow createRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        row.addAll(Arrays.asList(buttons));
        return row;
    }

    private void sendHoroscope(long chatId, String zodiacSign) {
        String sign = getEnglishSign(zodiacSign);
        String horoscope = horoscopeService.getHoroscope(sign);

        String userLanguage = userLanguageMap.getOrDefault(chatId, "ru");
        String translatedHoroscope = translationService.translate(horoscope, userLanguage);

        sendTextMessage(chatId, "🔮 " + translatedHoroscope);
    }

    private String getEnglishSign(String sign) {
        Map<String, String> zodiacMap = new HashMap<>();
        zodiacMap.put("♈ Овен", "aries");
        zodiacMap.put("♉ Телец", "taurus");
        zodiacMap.put("♊ Близнецы", "gemini");
        zodiacMap.put("♋ Рак", "cancer");
        zodiacMap.put("♌ Лев", "leo");
        zodiacMap.put("♍ Дева", "virgo");
        zodiacMap.put("♎ Весы", "libra");
        zodiacMap.put("♏ Скорпион", "scorpio");
        zodiacMap.put("♐ Стрелец", "sagittarius");
        zodiacMap.put("♑ Козерог", "capricorn");
        zodiacMap.put("♒ Водолей", "aquarius");
        zodiacMap.put("♓ Рыбы", "pisces");

        return zodiacMap.getOrDefault(sign, sign);
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
