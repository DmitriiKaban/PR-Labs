package com.app.lab2.services;

import com.app.lab2.models.ChatMessage;
import com.app.lab2.repositories.ChatMessagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessagesService {

    private final ChatMessagesRepository chatMessageRepository;

    public List<ChatMessage> findAllMessages() {
        return chatMessageRepository.findAll();  // Retrieve all messages from the database
    }
}
