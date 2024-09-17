package com.message.messi.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.message.messi.exception.InvalidRangeException;
import com.message.messi.exception.MessageNotFoundException;
import com.message.messi.model.Message;

@Component
public class MessageReceiver {

    private List<Message> receivedMessages = new ArrayList<>();

    @RabbitListener(queues = "messageQueue")
    public void receiveMessage(Message message) {
        receivedMessages.add(message);
        System.out.println("Received message: " + message.getContent() + " from sender " + message.getSenderName()
                + " to recipient " + message.getRecipientName());
    }

    public List<Message> getReceivedMessages() {
        return receivedMessages;
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
