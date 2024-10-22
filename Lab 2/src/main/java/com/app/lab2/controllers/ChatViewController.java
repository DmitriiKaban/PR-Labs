package com.app.lab2.controllers;

import com.app.lab2.models.ChatMessage;
import com.app.lab2.services.ChatMessagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

//    private final ChatMessagesService chatMessagesService;

//    @GetMapping("/chat-room")
//    public String chatRoom(Model model) {
////        List<ChatMessage> messages = chatMessagesService.findAllMessages();
////        model.addAttribute("messages", messages);
//        return "chat";
//    }
}

