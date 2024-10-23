package com.app.lab2.controllers;

import com.app.lab2.models.ChatMessage;
import com.app.lab2.repositories.ChatMessagesRepository;
import com.app.lab2.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessagesRepository chatMessageRepository;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {

        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        chatMessageService.processChatMessage(message);

        return message;
    }

    @GetMapping("/chat/messages")
    @ResponseBody
    public List<ChatMessage> getMessages() {
        return chatMessageRepository.findAll();
    }

    @GetMapping("/chat-room")
    public String chatRoom(Model model) {
        return "chat";
    }
}
