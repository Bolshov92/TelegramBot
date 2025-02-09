package com.example.TelegramBot.service;

import com.example.TelegramBot.entity.User;
import com.example.TelegramBot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isUserRegistered(Long chatId) {
        return userRepository.findByChatId(chatId).isPresent();
    }

    public void registerUser(Long chatId, String firstName, String lastName, String username) {
        if (isUserRegistered(chatId)) {
            return;
        }

        User newUser = new User(chatId, firstName, lastName, username, LocalDateTime.now());
        userRepository.save(newUser);
    }
}
