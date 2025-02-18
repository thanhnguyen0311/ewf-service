package com.danny.ewf_service.utils;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class OpenAIClient {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey = "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";
    private final ObjectMapper mapper;

    public OpenAIClient() {
        this.mapper = new ObjectMapper();
    }

    public String generateTitle() throws IOException {
        // Replace this placeholder key with a secure method for storing and retrieving your actual API key
        List<ChatMessage> messages = new ArrayList<>();

        ArrayNode content = mapper.createArrayNode();

        // Add text prompt
        ObjectNode textContent = mapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", "What's in this image?");
        content.add(textContent);

        // Add image URL
        ObjectNode imageContent = mapper.createObjectNode();
        imageContent.put("type", "image_url");
        ObjectNode imageUrl_node = mapper.createObjectNode();
        imageUrl_node.put("url", "https://st4.depositphotos.com/14431644/22076/i/450/depositphotos_220767694-stock-photo-handwriting-text-writing-example-concept.jpg");
        imageContent.set("image_url", imageUrl_node);
        content.add(imageContent);
        System.out.println(content.toString());

        String apiKey = "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";
        OpenAiService service = new OpenAiService(apiKey);
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