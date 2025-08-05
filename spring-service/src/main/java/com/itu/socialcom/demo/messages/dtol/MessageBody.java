package com.itu.socialcom.demo.messages.dtol;

import lombok.Data;

@Data
public class MessageBody {
    Long inboxId;
    String message;
    Long idMm;
    String platform;
}
