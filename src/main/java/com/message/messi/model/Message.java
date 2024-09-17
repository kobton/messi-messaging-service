package com.message.messi.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Message {

    @NotBlank(message = "Sender name cannot be null or empty")
    private String senderName;

    @NotBlank(message = "Sender name cannot be null or empty")
    private String recipientName;

    @NotBlank(message = "Message content cannot be null or empty")
    private String content;

}
