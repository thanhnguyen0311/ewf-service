package com.danny.ewf_service.utils;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.util.ArrayList;
import java.util.List;


@Service
public class OpenAIClient {

    private final ObjectMapper mapper;

    public OpenAIClient() {
        this.mapper = new ObjectMapper();
    }

    public String generateTitleFromImage(String userContent, String imageUrl) {
        // Replace this placeholder key with a secure method for storing and retrieving your actual API key
        List<ChatMessage> messages = new ArrayList<>();

        ArrayNode content = mapper.createArrayNode();

        // Add text prompt
        ObjectNode textContent = mapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", userContent);
        content.add(textContent);

        // Add image URL
        ObjectNode imageContent = mapper.createObjectNode();
        imageContent.put("type", "image_url");
        ObjectNode imageUrl_node = mapper.createObjectNode();
        imageUrl_node.put("url", imageUrl);
        imageContent.set("image_url", imageUrl_node);
        content.add(imageContent);

        OpenAiService service = new OpenAiService("openkey");
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), content.toString());
        messages.add(userMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o")
                .messages(messages)
                .build();

        ChatMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
        return responseMessage.getContent();
    }

}