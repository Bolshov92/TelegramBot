package com.example.TelegramBot.service;

import com.example.TelegramBot.entity.User;
import com.example.TelegramBot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
        Optional<User> existingUser = userRepository.findByChatId(chatId);
        if (existingUser.isEmpty()) {
            User newUser = new User(chatId, firstName, lastName, username, null, null, LocalDateTime.now());
            userRepository.save(newUser);
        } else {
            System.out.println("User already registered with language: " + existingUser.get().getLanguage());
        }
    }

    public void setLanguage(Long chatId, String language) {
        Optional<User> userOptional = userRepository.findByChatId(chatId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setLanguage(language);
            userRepository.save(user);
        }
    }

}
