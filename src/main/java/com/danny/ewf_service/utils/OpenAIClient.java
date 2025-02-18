package com.danny.ewf_service.utils;

import com.danny.ewf_service.configuration.DatasourceConfig;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class OpenAIClient {

    @Autowired
    private final DatasourceConfig datasourceConfig;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper mapper;

    public OpenAIClient(DatasourceConfig datasourceConfig) {
        this.datasourceConfig = datasourceConfig;
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

        OpenAiService service = new OpenAiService(datasourceConfig.getOpenAIkey());
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