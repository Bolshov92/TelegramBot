package com.example.TelegramBot.service;

import com.example.TelegramBot.config.BotConfig;
import com.example.TelegramBot.entity.User;
import com.example.TelegramBot.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class HoroscopeBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final HoroscopeService horoscopeService;
    private final TranslationService translationService;
    private final UserService userService;
    private final UserRepository userRepository;

    public HoroscopeBot(BotConfig config, HoroscopeService horoscopeService, TranslationService translationService, UserService userService, UserRepository userRepository) {
        this.config = config;
        this.horoscopeService = horoscopeService;
        this.translationService = translationService;
        this.userService = userService;
        this.userRepository = userRepository;
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
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String username = update.getMessage().getFrom().getUserName();

            Optional<User> optionalUser = userRepository.findByChatId(chatId);
            if (optionalUser.isEmpty()) {
                userService.registerUser(chatId, firstName, lastName, username);
                sendLanguageSelection(chatId);
            } else {
                User user = optionalUser.get();
                handleUserInput(chatId, messageText, user);
            }
        }
    }

    private void handleUserInput(Long chatId, String messageText, User user) {
        if (user.getLanguage() == null) {
            handleLanguageSelection(chatId, messageText, user);
        } else if (user.getSubscribedSign() == null) {
            handleZodiacSelection(chatId, messageText, user);
        } else {
            handleMenuSelection(chatId, messageText, user);
        }
    }

    private void handleLanguageSelection(Long chatId, String messageText, User user) {
        if (messageText.equals("🇷🇺 Русский") || messageText.equals("🇬🇧 English")) {
            String language = messageText.equals("🇷🇺 Русский") ? "ru" : "en";
            userService.setLanguage(chatId, language);  // Обновляем язык в БД

            System.out.println("Language selected: " + language);

            sendWelcomeMessage(chatId, language, user.getFirstName());
        } else {
            sendTextMessage(chatId, "🌍 Выберите язык, нажав на кнопку ниже.");
            sendLanguageSelection(chatId);
        }
    }


    private void handleZodiacSelection(Long chatId, String messageText, User user) {
        if (isZodiacSign(messageText)) {
            user.setSubscribedSign(messageText);
            userRepository.save(user);
            sendHoroscope(chatId, messageText, user.getLanguage());
            sendMenu(chatId, user.getLanguage());
        } else {
            sendZodiacKeyboard(chatId, user.getLanguage());
        }
    }

    private void handleMenuSelection(Long chatId, String messageText, User user) {
        switch (messageText) {
            case "🔄 Сменить подписку":
            case "🔄 Change Subscription":
                sendZodiacKeyboard(chatId, user.getLanguage());
                user.setSubscribedSign(null);
                userRepository.save(user);

                if (user.getLanguage() == null) {
                    sendLanguageSelection(chatId);
                }
                break;

            case "❌ Отписаться":
            case "❌ Unsubscribe":
                user.setSubscribedSign(null);
                user.setLanguage(null);
                userRepository.save(user);
                sendTextMessage(chatId, "🚫 Вы отписались от гороскопа. Пожалуйста, выберите язык.");
                sendLanguageSelection(chatId);
                break;

            case "🇷🇺 Русский":
            case "🇬🇧 English":
                String language = messageText.equals("🇷🇺 Русский") ? "ru" : "en";
                userService.setLanguage(chatId, language);
                sendTextMessage(chatId, language.equals("ru") ? "🇷🇺 Язык изменен на русский." : "🇬🇧 Language changed to English.");
                break;

            default:
                sendHoroscope(chatId, user.getSubscribedSign(), user.getLanguage());
                sendMenu(chatId, user.getLanguage());
                break;
        }
    }


    private void sendMenu(Long chatId, String language) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(language.equals("ru")
                ? "📌 Вы подписаны на ежедневный гороскоп!\nВыберите действие:"
                : "📌 You are subscribed to the daily horoscope!\nChoose an action:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(language.equals("ru") ? "🔄 Сменить подписку" : "🔄 Change Subscription");
        row1.add(language.equals("ru") ? "❌ Отписаться" : "❌ Unsubscribe");

        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
    }

    private void sendLanguageSelection(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🌍 Выберите язык / Choose a language:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("🇷🇺 Русский");
        row.add("🇬🇧 English");

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
    }

    private void sendWelcomeMessage(Long chatId, String language, String firstName) {
        String text = language.equals("ru")
                ? "Привет, " + firstName + "! Добро пожаловать! Выберите знак зодиака:"
                : "Hello, " + firstName + "! Welcome! Choose your zodiac sign:";

        sendTextMessage(chatId, text);
        sendZodiacKeyboard(chatId, language);
    }

    public void sendHoroscope(long chatId, String zodiacSign, String language) {
        Optional<User> userOptional = userRepository.findByChatId(chatId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            String userLanguage = user.getLanguage() != null ? user.getLanguage() : "ru";

            String sign = getEnglishSign(zodiacSign);
            String horoscope = horoscopeService.getHoroscope(sign);

            String translatedHoroscope = translationService.translate(horoscope, userLanguage);
            sendTextMessage(chatId, "🔮 " + translatedHoroscope);
        }
    }

    private void sendZodiacKeyboard(Long chatId, String language) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(language.equals("ru")
                ? "🔮 Выберите ваш знак зодиака:"
                : "🔮 Choose your zodiac sign:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(language.equals("ru") ? "♈ Овен" : "♈ Aries");
        row1.add(language.equals("ru") ? "♉ Телец" : "♉ Taurus");
        row1.add(language.equals("ru") ? "♊ Близнецы" : "♊ Gemini");
        row1.add(language.equals("ru") ? "♋ Рак" : "♋ Cancer");

        KeyboardRow row2 = new KeyboardRow();
        row2.add(language.equals("ru") ? "♌ Лев" : "♌ Leo");
        row2.add(language.equals("ru") ? "♍ Дева" : "♍ Virgo");
        row2.add(language.equals("ru") ? "♎ Весы" : "♎ Libra");
        row2.add(language.equals("ru") ? "♏ Скорпион" : "♏ Scorpio");

        KeyboardRow row3 = new KeyboardRow();
        row3.add(language.equals("ru") ? "♐ Стрелец" : "♐ Sagittarius");
        row3.add(language.equals("ru") ? "♑ Козерог" : "♑ Capricorn");
        row3.add(language.equals("ru") ? "♒ Водолей" : "♒ Aquarius");
        row3.add(language.equals("ru") ? "♓ Рыбы" : "♓ Pisces");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        sendMessage(message);
    }

    private boolean isZodiacSign(String text) {
        return List.of(
                        "♈ Овен", "♈ Aries",
                        "♉ Телец", "♉ Taurus",
                        "♊ Близнецы", "♊ Gemini",
                        "♋ Рак", "♋ Cancer",
                        "♌ Лев", "♌ Leo",
                        "♍ Дева", "♍ Virgo",
                        "♎ Весы", "♎ Libra",
                        "♏ Скорпион", "♏ Scorpio",
                        "♐ Стрелец", "♐ Sagittarius",
                        "♑ Козерог", "♑ Capricorn",
                        "♒ Водолей", "♒ Aquarius",
                        "♓ Рыбы", "♓ Pisces")
                .contains(text);
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
