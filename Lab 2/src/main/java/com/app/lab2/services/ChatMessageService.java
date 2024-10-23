package com.app.lab2.services;

import com.app.lab2.models.ChatMessage;
import com.app.lab2.repositories.ChatMessagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessagesRepository chatMessageRepository;

    @Async // annotation to run in a separate thread
    public void processChatMessage(ChatMessage message) {

        chatMessageRepository.save(message);
    }
}
