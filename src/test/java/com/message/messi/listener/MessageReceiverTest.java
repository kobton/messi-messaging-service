package com.message.messi.listener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.message.messi.exception.InvalidRangeException;
import com.message.messi.exception.MessageNotFoundException;
import com.message.messi.model.Message;

public class MessageReceiverTest {

    private MessageReceiver messageReceiver;

    @BeforeEach
    void setUp() {
        messageReceiver = new MessageReceiver();
    }

    @Test
    void testReceiveMessage() {
        Message message = new Message("Jakob", "Kajsa", "Hello World");
        messageReceiver.receiveMessage(message);

        List<Message> receivedMessages = messageReceiver.getReceivedMessages();
        assertEquals(1, receivedMessages.size());
        assertEquals("Jakob", receivedMessages.get(0).getSenderName());
        assertEquals("Hello World", receivedMessages.get(0).getContent());
    }

    @Test
    void testGetReceivedMessages() {
        Message message1 = new Message("Jakob", "Kajsa", "Message 1");
        Message message2 = new Message("Kajsa", "Jakob", "Message 2");
        messageReceiver.receiveMessage(message1);
        messageReceiver.receiveMessage(message2);

        List<Message> receivedMessages = messageReceiver.getReceivedMessages();
        assertEquals(2, receivedMessages.size());
        assertEquals("Jakob", receivedMessages.get(0).getSenderName());
        assertEquals("Kajsa", receivedMessages.get(1).getSenderName());
    }

    @Test
    void testDeleteMessageByRange_ValidRange() {
        messageReceiver.receiveMessage(new Message("Jakob", "Kajsa", "Message 1"));
        messageReceiver.receiveMessage(new Message("Kajsa", "Jakob", "Message 2"));
        messageReceiver.receiveMessage(new Message("Mattias", "Jakob", "Message 3"));

        boolean deleted = messageReceiver.deleteMessagesByRange(1, 2);
        assertTrue(deleted);

        List<Message> receivedMessages = messageReceiver.getReceivedMessages();
        assertEquals(1, receivedMessages.size());
        assertEquals("Jakob", receivedMessages.get(0).getSenderName());
    }

    @Test
    void testDeleteMessagesByRange_InvalidRange() {
        int invalidStart = 10;
        int invalidStop = 15;

        InvalidRangeException exception = assertThrows(InvalidRangeException.class, () -> {
            messageReceiver.deleteMessagesByRange(invalidStart, invalidStop);
        });

        assertEquals("Invalid range: start=" + invalidStart + ", stop=" + invalidStop, exception.getMessage());
    }

    @Test
    void testDeleteMessageByIndex_MessageNotFound() {
        int invalidIndex = 5;

        MessageNotFoundException exception = assertThrows(MessageNotFoundException.class, () -> {
            messageReceiver.deleteMessageByIndex(invalidIndex);
        });

        assertEquals("Message not found at index: " + invalidIndex, exception.getMessage());
    }

}