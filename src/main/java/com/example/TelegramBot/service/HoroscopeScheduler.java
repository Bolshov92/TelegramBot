package com.example.TelegramBot.service;

import com.example.TelegramBot.entity.User;
import com.example.TelegramBot.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class HoroscopeScheduler {
    private final HoroscopeBot horoscopeBot;
    private final UserRepository userRepository;

    public HoroscopeScheduler(HoroscopeBot horoscopeBot, UserRepository userRepository) {
        this.horoscopeBot = horoscopeBot;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void testSchedulerStart() {
        System.out.println("✅ Планировщик запущен! Текущее время (Лондон): " +
                LocalDateTime.now(ZoneId.of("Europe/London")));
    }

    @Scheduled(cron = "0 0 7 * * ?", zone = "Europe/London")
    public void sendDailyHoroscopes() {
        System.out.println("📢 [Scheduler] Отправка гороскопов в 07:00 утра (Лондон)...");
        sendHoroscopes();
    }

    private void sendHoroscopes() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getSubscribedSign() != null) {
                try {
                    horoscopeBot.sendHoroscope(user.getChatId(), user.getSubscribedSign(), user.getLanguage());
                    System.out.println("✔️ Гороскоп отправлен пользователю: " + user.getChatId());
                } catch (Exception e) {
                    System.err.println("❌ Ошибка при отправке гороскопа пользователю " + user.getChatId() + ": " + e.getMessage());
                }
            }
        }
    }
}
