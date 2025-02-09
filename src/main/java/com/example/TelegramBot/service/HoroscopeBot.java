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
    private final UserRepository userRepository;
    private final Map<Long, String> userLanguageMap = new HashMap<>();

    public HoroscopeBot(BotConfig config, HoroscopeService horoscopeService, TranslationService translationService, UserRepository userRepository) {
        this.config = config;
        this.horoscopeService = horoscopeService;
        this.translationService = translationService;
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
            String username = update.getMessage().getChat().getUserName();
            String firstName = update.getMessage().getChat().getFirstName();
            String lastName = update.getMessage().getChat().getLastName();

            if (!isUserRegistered(chatId)) {
                registerUser(chatId, username, firstName, lastName);
                sendLanguageSelection(chatId);
            } else {
                handleUserCommands(chatId, messageText);
            }
        }
    }

    private boolean isUserRegistered(long chatId) {
        return userRepository.findByChatId(chatId).isPresent();
    }

    private void registerUser(long chatId, String username, String firstName, String lastName) {
        if (!isUserRegistered(chatId)) {
            User newUser = new User(chatId, firstName, lastName, username, LocalDateTime.now());
            userRepository.save(newUser);
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

    private void handleUserCommands(long chatId, String messageText) {
        if (!userLanguageMap.containsKey(chatId)) {
            handleLanguageSelection(chatId, messageText);
            return;
        }

        String userLanguage = userLanguageMap.get(chatId);

        switch (messageText) {
            case "🔮 Гороскоп":
                sendZodiacKeyboard(chatId, userLanguage);
                break;
            default:
                if (isZodiacSign(messageText)) {
                    sendHoroscope(chatId, messageText);
                } else {
                    sendTextMessage(chatId, userLanguage.equals("ru") ? "Пожалуйста, выберите вариант ⬇" :
                            "Please choose an option ⬇");
                }
                break;
        }
    }

    private void handleLanguageSelection(long chatId, String messageText) {
        Optional<User> userOptional = userRepository.findByChatId(chatId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String firstName = user.getFirstName();
            String lastName = user.getLastName();

            switch (messageText) {
                case "🇷🇺 Русский":
                    userLanguageMap.put(chatId, "ru");
                    sendWelcomeMessage(chatId, firstName, lastName, "ru");
                    break;
                case "🇬🇧 English":
                    userLanguageMap.put(chatId, "en");
                    sendWelcomeMessage(chatId, firstName, lastName, "en");
                    break;
                default:
                    sendLanguageSelection(chatId);
                    break;
            }
        } else {
            sendLanguageSelection(chatId);
        }
    }


    private void sendWelcomeMessage(long chatId, String firstName, String lastName, String language) {
        String fullName = (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");

        String text = language.equals("ru") ?
                String.format("Привет, %s! 👋 Добро пожаловать в бот гороскопов!\n\nВыберите ваш знак зодиака:", fullName) :
                String.format("Hello, %s! 👋 Welcome to the Horoscope Bot!\n\nChoose your zodiac sign:", fullName);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(getZodiacKeyboard(language));
        sendMessage(message);
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

    private boolean isZodiacSign(String text) {
        return List.of("♈ Овен", "♉ Телец", "♊ Близнецы", "♋ Рак", "♌ Лев", "♍ Дева",
                        "♎ Весы", "♏ Скорпион", "♐ Стрелец", "♑ Козерог", "♒ Водолей", "♓ Рыбы",
                        "♈ Aries", "♉ Taurus", "♊ Gemini", "♋ Cancer", "♌ Leo", "♍ Virgo",
                        "♎ Libra", "♏ Scorpio", "♐ Sagittarius", "♑ Capricorn", "♒ Aquarius", "♓ Pisces")
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
