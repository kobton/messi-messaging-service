package com.message.messi.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.message.messi.model.Message;

@Service
public class MessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(Message message) {
        rabbitTemplate.convertAndSend("messageExchange", "messageRoutingKey", message);
        System.out.println("Message sent: " + message.getContent() + " from sender " + message.getSenderName()+" to recipient "+message.getRecipientName());
    }

}
