package com.group.cs520.controller;

import com.group.cs520.model.Message;
import com.group.cs520.service.ChatService;
import com.group.cs520.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatService chatService;

    @MessageMapping("/sendMessage")
    @SendTo("/room/messages")
    public Message sendMessage(@Payload Message chatMessage) {
        // Save the message
        Message savedMessage = messageRepository.save(chatMessage);

        // Handle conversation
        chatService.handleConversationForMessage(savedMessage);

        return savedMessage;
    }
}