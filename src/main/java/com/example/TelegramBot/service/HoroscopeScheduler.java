package com.example.TelegramBot.service;

import com.example.TelegramBot.entity.User;
import com.example.TelegramBot.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HoroscopeScheduler {
    private final HoroscopeBot horoscopeBot;
    private final UserRepository userRepository;

    public HoroscopeScheduler(HoroscopeBot horoscopeBot, UserRepository userRepository) {
        this.horoscopeBot = horoscopeBot;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendDailyHoroscopes() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getSubscribedSign() != null) {
                horoscopeBot.sendHoroscope(user.getChatId(), user.getSubscribedSign(), user.getLanguage());
            }
        }
    }
}
