package com.example.TelegramBot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long chatId;

    private String firstName;
    private String lastName;
    private String username;

    @Column(name = "language")
    private String language;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "subscribed_sign")
    private String subscribedSign;

    public User() {}

    public User(Long chatId, String firstName, String lastName, String username, String language, String subscribedSign, LocalDateTime registeredAt) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.language = language;
        this.subscribedSign = subscribedSign;
        this.registeredAt = registeredAt;
    }


    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getLanguage() { return language; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public String getSubscribedSign() { return subscribedSign; }

    public void setId(Long id) { this.id = id; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUsername(String username) { this.username = username; }
    public void setLanguage(String language) { this.language = language; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public void setSubscribedSign(String subscribedSign) { this.subscribedSign = subscribedSign; }
}
