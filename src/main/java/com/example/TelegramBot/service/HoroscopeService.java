package com.example.TelegramBot.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HoroscopeService {
    private static final String API_URL = "https://best-daily-astrology-and-horoscope-api.p.rapidapi.com/api/Detailed-Horoscope/";
    @Value("${horoscope.api.key}")
    private String apiKey;

    @Value("${horoscope.api.host}")
    private String apiHost;

    public String getHoroscope(String sign) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", apiHost);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String requestUrl = API_URL + "?zodiacSign=" + sign.toLowerCase();

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl, HttpMethod.GET, entity, String.class
            );


            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject jsonResponse = new JSONObject(response.getBody());


                System.out.println("API Response: " + jsonResponse.toString(4));


                if (jsonResponse.has("prediction")) {
                    return "🔮 Гороскоп для " + sign + ":\n\n" + jsonResponse.getString("prediction");
                } else {
                    return "Ошибка: API не вернул предсказание!";
                }
            } else {
                return "Ошибка получения гороскопа: " + response.getStatusCode();
            }

        } catch (Exception e) {
            return "Ошибка при запросе API: " + e.getMessage();
        }
    }
}
