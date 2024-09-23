package com.message.messi.listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.message.messi.dto.MessageRequest;
import com.message.messi.exception.InvalidRangeException;
import com.message.messi.exception.MessageNotFoundException;
import com.message.messi.model.Message;
import com.message.messi.repository.MessageRepository;

@Component
public class MessageReceiver {

    private List<Message> receivedMessages = new ArrayList<>();

    @Autowired
    MessageRepository messageRepository;

    @RabbitListener(queues = "messageQueue")
    public void receiveMessage(MessageRequest messageRequest) {

        Message message = new Message();

        message.setContent(messageRequest.getContent());
        message.setRecipientName(messageRequest.getRecipientName());
        message.setSenderName(messageRequest.getSenderName());
        message.setTimestamp(LocalDateTime.now());
        receivedMessages.add(message);
        System.out.println(
                "Received message: " + messageRequest.getContent() + " from sender " + messageRequest.getSenderName()
                        + " to recipient " + messageRequest.getRecipientName());

        messageRepository.save(message);
    }

    public List<Message> getReceivedMessages() {

        List<Message> messages;

        return messageRepository.findAll();
    }

    public List<Message> getMessagesForRecipient(String recipientName) {
        return receivedMessages.stream()
                .filter(message -> message.getRecipientName().equalsIgnoreCase(recipientName))
                .collect(Collectors.toList());
    }

    public boolean deleteMessageByIndex(int index) {
        if (index >= 0 && index < receivedMessages.size()) {
            receivedMessages.remove(index);
            return true;
        }
        throw new MessageNotFoundException("Message not found at index: " + index);
    }

    public boolean deleteMessagesByRange(int start, int stop) {
        if (start < 0 || stop >= receivedMessages.size() || start > stop) {
            throw new InvalidRangeException("Invalid range: start=" + start + ", stop=" + stop);
        }
        receivedMessages.subList(start, stop + 1).clear();
        return true;
    }

    public List<Message> getMessagesByRange(int start, int stop) {
        if (start < 0 || stop >= receivedMessages.size() || start > stop) {
            throw new InvalidRangeException("Invalid range: start=" + start + ", stop=" + stop);
        }
        return receivedMessages.subList(start, stop + 1);
    }
}
