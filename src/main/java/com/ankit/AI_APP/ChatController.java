package com.ankit.AI_APP;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chat")
@CrossOrigin("*")
public class ChatController {

    private final HuggingFaceChatService chatService;

    public ChatController(HuggingFaceChatService chatService) {
        this.chatService = chatService;
    }

    public static class ChatRequest {
        public String model;
        public String prompt;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    @PostMapping
    public String askModel(@RequestBody ChatRequest request) {
        return chatService.chat(request.getModel(), request.getPrompt());
    }


    @PostMapping("/reset")
    public String resetConversation() {
        chatService.resetConversation();
        return "Conversation reset successfully!";
    }
}
