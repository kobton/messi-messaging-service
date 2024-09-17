package com.message.messi.controller;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.message.messi.exception.GlobalExceptionHandler;
import com.message.messi.exception.InvalidRangeException;
import com.message.messi.exception.MessageNotFoundException;
import com.message.messi.listener.MessageReceiver;
import com.message.messi.model.Message;
import com.message.messi.service.MessageSender;

public class MessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageSender messageSender;

    @Mock
    private MessageReceiver messageReceiver;

    @InjectMocks
    private MessageController messageController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testSendMessage() throws Exception {

        Message request = new Message("Jakob", "Kajsa", "Hello there");

        mockMvc.perform(post("/api/messages/send")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Message sent successfully"));

        verify(messageSender, times(1)).sendMessage(any(Message.class));
    }

    @Test
    void testGetReceivedMessages() throws Exception {
        Message message1 = new Message("Jakob", "Kajsa", "Message 1");
        Message message2 = new Message("Kajsa", "Jakob", "Message 2");
        when(messageReceiver.getReceivedMessages()).thenReturn(Arrays.asList(message1, message2));

        mockMvc.perform(get("/api/messages/received"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderName").value("Jakob"))
                .andExpect(jsonPath("$[1].senderName").value("Kajsa"));

        verify(messageReceiver, times(1)).getReceivedMessages();
    }

    @Test
    void testDeleteMessagesByRange() throws Exception {
        when(messageReceiver.deleteMessagesByRange(0, 1)).thenReturn(true);

        mockMvc.perform(delete("/api/messages/delete/range")
                .param("start", "0")
                .param("stop", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Messages from index 0 to 1 deleted."));

        verify(messageReceiver, times(1)).deleteMessagesByRange(0, 1);
    }

    @Test
    void testDeleteMessageByIndex_NotFound() throws Exception {
        doThrow(new MessageNotFoundException("Message not found at index: 2")).when(messageReceiver)
                .deleteMessageByIndex(2);

        mockMvc.perform(delete("/api/messages/delete/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Message not found at index: 2"));

        verify(messageReceiver, times(1)).deleteMessageByIndex(2);
    }

    @Test
    void testDeleteMessagesByRange_InvalidRange() throws Exception {
        doThrow(new InvalidRangeException("Invalid range: start=10, stop=0")).when(messageReceiver)
                .deleteMessagesByRange(10, 0);

        mockMvc.perform(delete("/api/messages/delete/range")
                .param("start", "10")
                .param("stop", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid range: start=10, stop=0"));

        verify(messageReceiver, times(1)).deleteMessagesByRange(10, 0);
    }

    @Test
    public void getMessagesForRecipient_validRecipient_shouldReturnMessages() throws Exception {
        List<Message> mockMessages = Arrays.asList(
                new Message("Jakob", "Kajsa", "Hello Kajsa"),
                new Message("Mattias", "Kajsa", "Message for Kajsa"));

        Mockito.when(messageReceiver.getMessagesForRecipient(anyString())).thenReturn(mockMessages);

        mockMvc.perform(get("/api/messages/received/recipient")
                .param("name", "Kajsa")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].senderName", is("Jakob")))
                .andExpect(jsonPath("$[0].recipientName", is("Kajsa")))
                .andExpect(jsonPath("$[0].content", is("Hello Kajsa")))
                .andExpect(jsonPath("$[1].senderName", is("Mattias")))
                .andExpect(jsonPath("$[1].recipientName", is("Kajsa")))
                .andExpect(jsonPath("$[1].content", is("Message for Kajsa")));
    }

    @Test
    public void getMessagesForRecipient_noMessagesFound_shouldReturn204() throws Exception {
        Mockito.when(messageReceiver.getMessagesForRecipient(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/received/recipient")
                .param("name", "UnknownRecipient")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
