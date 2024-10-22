package com.app.lab2.repositories;

import com.app.lab2.models.ChatMessage;
import com.app.lab2.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessage, Long> {

}
