package com.message.messi.listener;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.message.messi.dto.MessageRequest;
import com.message.messi.exception.InvalidRangeException;
import com.message.messi.exception.MessageNotFoundException;
import com.message.messi.model.Message;
import com.message.messi.repository.MessageRepository;

@ExtendWith(MockitoExtension.class)
public class MessageReceiverTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageReceiver messageReceiver;

    private Message message;

    @BeforeEach
    void setUp() {
        message = new Message(Long.valueOf(1), "Jakob", "Kajsa", "Hello World", LocalDateTime.now());
    }

    @Test
    void testReceiveMessage() {
        // Message message = new Message(Long.valueOf(1), "Jakob", "Kajsa", "Hello
        // world", LocalDateTime.now());
        MessageRequest messageRequest = new MessageRequest("Jakob", "Kajsa", "Hello world");

        Mockito.when(messageRepository.findAll()).thenReturn(Arrays.asList(message));
        Mockito.when(messageRepository.save(any(Message.class))).thenReturn(message);

        messageReceiver.receiveMessage(messageRequest);

        List<Message> receivedMessages = messageReceiver.getReceivedMessages();
        assertEquals(1, receivedMessages.size());
        assertEquals("Jakob", receivedMessages.get(0).getSenderName());
        assertEquals("Hello World", receivedMessages.get(0).getContent());
    }

    @Test
    void testGetReceivedMessages() {
        MessageRequest messageReq1 = new MessageRequest("Jakob", "Kajsa", "Message 1");
        MessageRequest messageReq2 = new MessageRequest("Kajsa", "Jakob", "Message 2");

        messageReceiver.receiveMessage(messageReq1);
        messageReceiver.receiveMessage(messageReq2);

        Message message1 = new Message(Long.valueOf(1), "Jakob", "Kajsa", "Message 1", LocalDateTime.now());
        Message message2 = new Message(Long.valueOf(2), "Kajsa", "Jakob", "Message 2", LocalDateTime.now());

        when(messageRepository.findAll()).thenReturn(Arrays.asList(message1, message2));

        List<Message> receivedMessages = messageReceiver.getReceivedMessages();
        assertEquals(2, receivedMessages.size());
        assertEquals("Jakob", receivedMessages.get(0).getSenderName());
        assertEquals("Kajsa", receivedMessages.get(1).getSenderName());
    }

    @Test
    void testDeleteMessageByRange_ValidRange() {
        messageReceiver.receiveMessage(new MessageRequest("Jakob", "Kajsa", "Message 1"));
        messageReceiver.receiveMessage(new MessageRequest("Kajsa", "Jakob", "Message 2"));
        messageReceiver.receiveMessage(new MessageRequest("Mattias", "Jakob", "Message 3"));

        boolean deleted = messageReceiver.deleteMessagesByRange(1, 2);
        assertTrue(deleted);

        Message message2 = new Message(Long.valueOf(2), "Kajsa", "Jakob", "Message 2", LocalDateTime.now());

        when(messageRepository.findAll()).thenReturn(Arrays.asList(message2));

        List<Message> receivedMessages = messageReceiver.getReceivedMessages();
        assertEquals(1, receivedMessages.size());
        assertEquals("Kajsa", receivedMessages.get(0).getSenderName());
    }

    @Test
    void testDeleteMessagesByRange_InvalidRange() {
        int invalidStart = 10;
        int invalidStop = 15;

        InvalidRangeException exception = assertThrows(InvalidRangeException.class,
                () -> {
                    messageReceiver.deleteMessagesByRange(invalidStart, invalidStop);
                });

        assertEquals("Invalid range: start=" + invalidStart + ", stop=" +
                invalidStop, exception.getMessage());
    }

    @Test
    void testDeleteMessageByIndex_MessageNotFound() {
        int invalidIndex = 5;

        MessageNotFoundException exception = assertThrows(MessageNotFoundException.class, () -> {
            messageReceiver.deleteMessageByIndex(invalidIndex);
        });

        assertEquals("Message not found at index: " + invalidIndex,
                exception.getMessage());
    }

}