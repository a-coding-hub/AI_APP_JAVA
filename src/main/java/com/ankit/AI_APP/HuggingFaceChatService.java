package com.ankit.AI_APP;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class HuggingFaceChatService {

    @Value("${hf.api.key}")
    private String apiKey;

    @Value("${hf.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // Store conversation
    private final List<Map<String, String>> conversation = new ArrayList<>();

    public void resetConversation() {
        conversation.clear();
    }

    // Main chat entry
    public String chat(String model, String userMessage) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return sendChatRequest(model, userMessage);
            } catch (Exception e) {
                attempt++;
                System.out.println("Attempt " + attempt + " failed: " + e.getMessage());

                if (attempt >= maxRetries) {
                    // ✅ Fallback: safe dummy reply
                    return getFallbackReply(userMessage, e.getMessage());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
        return getFallbackReply(userMessage, "Unexpected error.");
    }

    private String sendChatRequest(String model, String userMessage) throws Exception {
        // Add user message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        conversation.add(userMsg);

        // Build request
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", conversation);
        payload.put("stream", false);
        String body = mapper.writeValueAsString(payload);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("HF API error: " + response.getStatusCode() + " - " + response.getBody());
        }

        // Parse response
        Map<String, Object> jsonResponse = mapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonResponse.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String assistantReply = (String) message.get("content");

        // Add assistant reply
        Map<String, String> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", assistantReply);
        conversation.add(assistantMsg);

        return assistantReply;
    }

    // ✅ Fallback reply generator
    private String getFallbackReply(String userMessage, String reason) {
        return "🤖 Sorry, I’m currently offline (reason: " + reason + ").\n" +
                "But here’s a safe fallback response to your message:\n\n" +
                "👉 You said: \"" + userMessage + "\"\n" +
                "👉 My reply: I cannot connect to the AI service right now, please try again later.";
    }
}
