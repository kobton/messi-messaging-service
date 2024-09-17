package com.message.messi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.message.messi.exception.InvalidInputException;
import com.message.messi.exception.InvalidRangeException;
import com.message.messi.listener.MessageReceiver;
import com.message.messi.model.Message;
import com.message.messi.service.MessageSender;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private MessageReceiver messageReceiver;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@Valid @RequestBody Message message) {
        if (message.getSenderName() == null || message.getContent() == null || message.getRecipientName() == null) {
            throw new InvalidInputException("Sender and content must not be null");
        }
        messageSender.sendMessage(message);
        return ResponseEntity.ok("Message sent successfully");
    }

    @GetMapping("/received")
    public ResponseEntity<List<Message>> getReceivedMessages() {
        return ResponseEntity.ok(messageReceiver.getReceivedMessages());
    }

    @GetMapping("/received/recipient")
    public ResponseEntity<List<Message>> getMessagesForRecipient(
            @RequestParam @NotBlank(message = "Recipient must not be blank") String name) {
        List<Message> messages = messageReceiver.getMessagesForRecipient(name);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/received/range")
    public ResponseEntity<List<Message>> getMessagesByRange(@RequestParam int start, @RequestParam int stop) {
        return ResponseEntity.ok(messageReceiver.getMessagesByRange(start, stop));
    }

    @DeleteMapping("/delete/{index}")
    public ResponseEntity<String> deleteMessageByIndex(@PathVariable int index) {
        boolean deleted = messageReceiver.deleteMessageByIndex(index);
        if (deleted) {
            return ResponseEntity.ok("Message at index " + index + " deleted.");
        } else {
            throw new InvalidRangeException("Message not found at index " + index + ".");
        }
    }

    @DeleteMapping("/delete/range")
    public String deleteMessagesByRange(@RequestParam int start, @RequestParam int stop) {
        boolean deleted = messageReceiver.deleteMessagesByRange(start, stop);
        if (deleted) {
            return "Messages from index " + start + " to " + stop + " deleted.";
        } else {
            return "Invalid range or no messages to delete.";
        }
    }
}